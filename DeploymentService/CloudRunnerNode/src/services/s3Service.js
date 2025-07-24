const AWS = require('aws-sdk');
const fs = require('fs-extra');
const path = require('path');
const unzipper = require('unzipper');
const tar = require('tar');
const { v4: uuidv4 } = require('uuid');

class S3Service {
     constructor() {
          // Configure AWS SDK
          this.s3 = new AWS.S3({
               accessKeyId: process.env.AWS_ACCESS_KEY_ID,
               secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY,
               region: process.env.AWS_REGION || 'us-east-1'
          });
     }

     // Parse S3 URL to extract bucket and key
     parseS3Url(s3Url) {
          try {
               const url = new URL(s3Url);
               let bucket, key;

               if (url.hostname.includes('s3.amazonaws.com')) {
                    // Format: https://s3.amazonaws.com/bucket/key
                    const pathParts = url.pathname.split('/').filter(Boolean);
                    bucket = pathParts[0];
                    key = pathParts.slice(1).join('/');
               } else if (url.hostname.includes('s3-') || url.hostname.includes('s3.')) {
                    // Format: https://s3-region.amazonaws.com/bucket/key or https://s3.region.amazonaws.com/bucket/key
                    const pathParts = url.pathname.split('/').filter(Boolean);
                    bucket = pathParts[0];
                    key = pathParts.slice(1).join('/');
               } else if (url.hostname.endsWith('.s3.amazonaws.com')) {
                    // Format: https://bucket.s3.amazonaws.com/key
                    bucket = url.hostname.split('.')[0];
                    key = url.pathname.slice(1); // Remove leading slash
               } else if (url.hostname.includes('.s3-') || url.hostname.includes('.s3.')) {
                    // Format: https://bucket.s3-region.amazonaws.com/key
                    bucket = url.hostname.split('.')[0];
                    key = url.pathname.slice(1);
               } else {
                    throw new Error('Unsupported S3 URL format');
               }

               if (!bucket || !key) {
                    throw new Error('Could not parse bucket and key from S3 URL');
               }

               return { bucket, key };
          } catch (error) {
               throw new Error(`Invalid S3 URL: ${error.message}`);
          }
     }

     // Download file from S3
     async downloadFromS3(bucket, key, downloadPath) {
          try {
               console.log(`Downloading from S3: s3://${bucket}/${key}`);

               const params = {
                    Bucket: bucket,
                    Key: key
               };

               // Create download stream
               const downloadStream = this.s3.getObject(params).createReadStream();
               const writeStream = fs.createWriteStream(downloadPath);

               return new Promise((resolve, reject) => {
                    downloadStream.pipe(writeStream);

                    downloadStream.on('error', reject);
                    writeStream.on('error', reject);
                    writeStream.on('close', resolve);
               });
          } catch (error) {
               throw new Error(`Failed to download from S3: ${error.message}`);
          }
     }

     // Extract archive (zip or tar.gz)
     async extractArchive(archivePath, extractPath, archiveType) {
          try {
               console.log(`Extracting ${archiveType} archive: ${archivePath} to ${extractPath}`);

               await fs.ensureDir(extractPath);

               if (archiveType === 'zip') {
                    await new Promise((resolve, reject) => {
                         fs.createReadStream(archivePath)
                              .pipe(unzipper.Extract({ path: extractPath }))
                              .on('close', resolve)
                              .on('error', reject);
                    });
               } else if (archiveType === 'tar' || archiveType === 'tar.gz' || archiveType === 'tgz') {
                    await tar.extract({
                         file: archivePath,
                         cwd: extractPath,
                         strip: 1 // Remove top-level directory if present
                    });
               } else {
                    throw new Error(`Unsupported archive type: ${archiveType}`);
               }

               console.log(`Successfully extracted archive to: ${extractPath}`);
          } catch (error) {
               throw new Error(`Failed to extract archive: ${error.message}`);
          }
     }

     // Detect archive type from filename
     detectArchiveType(filename) {
          const lowerFilename = filename.toLowerCase();

          if (lowerFilename.endsWith('.zip')) {
               return 'zip';
          } else if (lowerFilename.endsWith('.tar.gz') || lowerFilename.endsWith('.tgz')) {
               return 'tar.gz';
          } else if (lowerFilename.endsWith('.tar')) {
               return 'tar';
          } else {
               // Default to zip if we can't determine
               return 'zip';
          }
     }

     // Generate project name from S3 key or create a unique name
     generateProjectName(s3Key) {
          const filename = path.basename(s3Key);
          const nameWithoutExt = filename.replace(/\.(zip|tar\.gz|tgz|tar)$/i, '');

          // Sanitize project name
          const sanitized = nameWithoutExt
               .replace(/[^a-zA-Z0-9-_]/g, '-')
               .replace(/-+/g, '-')
               .replace(/^-|-$/g, '');

          // Add timestamp to ensure uniqueness
          const timestamp = Date.now();
          return sanitized ? `${sanitized}-${timestamp}` : `project-${timestamp}`;
     }

     // Main method to download and extract project from S3
     async downloadAndExtract(s3Url, workspaceBase) {
          const tempDir = path.join('/tmp', uuidv4());

          try {
               // Parse S3 URL
               const { bucket, key } = this.parseS3Url(s3Url);

               // Generate project name
               const projectName = this.generateProjectName(key);
               const projectPath = path.join(workspaceBase, projectName);

               // Detect archive type
               const archiveType = this.detectArchiveType(key);

               // Create temp directory
               await fs.ensureDir(tempDir);

               // Download file
               const downloadPath = path.join(tempDir, path.basename(key));
               await this.downloadFromS3(bucket, key, downloadPath);

               // Extract archive
               await this.extractArchive(downloadPath, projectPath, archiveType);

               // Clean up temp files
               await fs.remove(tempDir);

               console.log(`Project successfully downloaded and extracted to: ${projectPath}`);
               return projectName;

          } catch (error) {
               // Clean up temp files on error
               try {
                    await fs.remove(tempDir);
               } catch (cleanupError) {
                    console.error('Failed to clean up temp files:', cleanupError);
               }

               throw error;
          }
     }

     // Alternative method using AWS CLI (if AWS SDK doesn't work)
     async downloadWithCLI(s3Url, workspaceBase) {
          const { exec } = require('child_process');
          const { promisify } = require('util');
          const execAsync = promisify(exec);

          try {
               const { bucket, key } = this.parseS3Url(s3Url);
               const projectName = this.generateProjectName(key);
               const tempDir = path.join('/tmp', uuidv4());
               const downloadPath = path.join(tempDir, path.basename(key));
               const projectPath = path.join(workspaceBase, projectName);

               await fs.ensureDir(tempDir);

               // Download using AWS CLI
               const downloadCommand = `aws s3 cp s3://${bucket}/${key} ${downloadPath}`;
               console.log(`Executing: ${downloadCommand}`);

               await execAsync(downloadCommand);

               // Extract archive
               const archiveType = this.detectArchiveType(key);
               await this.extractArchive(downloadPath, projectPath, archiveType);

               // Clean up
               await fs.remove(tempDir);

               console.log(`Project successfully downloaded and extracted to: ${projectPath}`);
               return projectName;

          } catch (error) {
               throw new Error(`AWS CLI download failed: ${error.message}`);
          }
     }

     // Upload project back to S3 (bonus feature)
     async uploadProject(projectPath, bucket, keyPrefix = '') {
          try {
               const archiver = require('archiver');
               const tempZipPath = path.join('/tmp', `${path.basename(projectPath)}-${Date.now()}.zip`);

               // Create zip archive
               await new Promise((resolve, reject) => {
                    const output = fs.createWriteStream(tempZipPath);
                    const archive = archiver('zip', { zlib: { level: 9 } });

                    output.on('close', resolve);
                    archive.on('error', reject);

                    archive.pipe(output);
                    archive.directory(projectPath, false);
                    archive.finalize();
               });

               // Upload to S3
               const uploadKey = keyPrefix ? `${keyPrefix}/${path.basename(tempZipPath)}` : path.basename(tempZipPath);
               const fileBuffer = await fs.readFile(tempZipPath);

               const params = {
                    Bucket: bucket,
                    Key: uploadKey,
                    Body: fileBuffer,
                    ContentType: 'application/zip'
               };

               const result = await this.s3.upload(params).promise();

               // Clean up temp file
               await fs.remove(tempZipPath);

               console.log(`Project uploaded to: ${result.Location}`);
               return result.Location;

          } catch (error) {
               throw new Error(`Failed to upload project: ${error.message}`);
          }
     }
}

module.exports = new S3Service(); 