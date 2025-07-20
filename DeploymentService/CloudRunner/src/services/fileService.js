const fs = require('fs-extra');
const path = require('path');

class FileService {
     constructor() {
          // No file size limit - handle files of any size
     }

     // Validate and sanitize file paths
     sanitizePath(basePath, filePath) {
          const fullPath = path.resolve(basePath, filePath);

          // Ensure the path is within the base directory (prevent directory traversal)
          if (!fullPath.startsWith(path.resolve(basePath))) {
               throw new Error('Invalid file path: Path traversal not allowed');
          }

          return fullPath;
     }

     // Read file or directory contents
     async readFile(basePath, filePath) {
          const fullPath = this.sanitizePath(basePath, filePath);

          try {
               const stats = await fs.stat(fullPath);

               if (stats.isDirectory()) {
                    const files = await fs.readdir(fullPath);
                    const fileDetails = await Promise.all(
                         files.map(async (file) => {
                              const fileFullPath = path.join(fullPath, file);
                              const fileStats = await fs.stat(fileFullPath);
                              return {
                                   name: file,
                                   isDirectory: fileStats.isDirectory(),
                                   size: fileStats.size,
                                   modified: fileStats.mtime,
                                   path: path.relative(basePath, fileFullPath)
                              };
                         })
                    );

                    return {
                         isDirectory: true,
                         files: fileDetails,
                         path: path.relative(basePath, fullPath)
                    };
               } else {
                    // Read file content without size restrictions
                    const content = await fs.readFile(fullPath, 'utf8');
                    return {
                         isDirectory: false,
                         content,
                         size: stats.size,
                         modified: stats.mtime,
                         path: path.relative(basePath, fullPath)
                    };
               }
          } catch (error) {
               if (error.code === 'ENOENT') {
                    throw new Error(`File or directory not found: ${filePath}`);
               }
               throw error;
          }
     }

     // Write content to a file
     async writeFile(basePath, filePath, content) {
          const fullPath = this.sanitizePath(basePath, filePath);

          try {
               // Ensure parent directory exists
               await fs.ensureDir(path.dirname(fullPath));

               // Write file
               await fs.writeFile(fullPath, content, 'utf8');

               console.log(`File written: ${fullPath}`);
               return { path: path.relative(basePath, fullPath) };
          } catch (error) {
               throw new Error(`Failed to write file: ${error.message}`);
          }
     }

     // Create a new file or directory
     async createFile(basePath, filePath, isDirectory = false) {
          const fullPath = this.sanitizePath(basePath, filePath);

          try {
               // Check if file/directory already exists
               if (await fs.pathExists(fullPath)) {
                    throw new Error(`File or directory already exists: ${filePath}`);
               }

               if (isDirectory) {
                    await fs.ensureDir(fullPath);
                    console.log(`Directory created: ${fullPath}`);
               } else {
                    // Ensure parent directory exists
                    await fs.ensureDir(path.dirname(fullPath));
                    // Create empty file
                    await fs.writeFile(fullPath, '', 'utf8');
                    console.log(`File created: ${fullPath}`);
               }

               return { path: path.relative(basePath, fullPath) };
          } catch (error) {
               throw new Error(`Failed to create ${isDirectory ? 'directory' : 'file'}: ${error.message}`);
          }
     }

     // Delete a file or directory
     async deleteFile(basePath, filePath) {
          const fullPath = this.sanitizePath(basePath, filePath);

          try {
               await fs.remove(fullPath);
               console.log(`Deleted: ${fullPath}`);
               return { path: path.relative(basePath, fullPath) };
          } catch (error) {
               if (error.code === 'ENOENT') {
                    throw new Error(`File or directory not found: ${filePath}`);
               }
               throw new Error(`Failed to delete: ${error.message}`);
          }
     }

     // Move/rename a file or directory
     async moveFile(basePath, sourcePath, destinationPath) {
          const sourceFullPath = this.sanitizePath(basePath, sourcePath);
          const destFullPath = this.sanitizePath(basePath, destinationPath);

          try {
               // Ensure destination directory exists
               await fs.ensureDir(path.dirname(destFullPath));

               await fs.move(sourceFullPath, destFullPath);
               console.log(`Moved: ${sourceFullPath} -> ${destFullPath}`);

               return {
                    source: path.relative(basePath, sourceFullPath),
                    destination: path.relative(basePath, destFullPath)
               };
          } catch (error) {
               throw new Error(`Failed to move file: ${error.message}`);
          }
     }

     // Copy a file or directory
     async copyFile(basePath, sourcePath, destinationPath) {
          const sourceFullPath = this.sanitizePath(basePath, sourcePath);
          const destFullPath = this.sanitizePath(basePath, destinationPath);

          try {
               await fs.copy(sourceFullPath, destFullPath);
               console.log(`Copied: ${sourceFullPath} -> ${destFullPath}`);

               return {
                    source: path.relative(basePath, sourceFullPath),
                    destination: path.relative(basePath, destFullPath)
               };
          } catch (error) {
               throw new Error(`Failed to copy file: ${error.message}`);
          }
     }

     // List all projects in the workspace
     async listProjects(basePath) {
          try {
               await fs.ensureDir(basePath);
               const items = await fs.readdir(basePath);

               const projects = await Promise.all(
                    items.map(async (item) => {
                         const itemPath = path.join(basePath, item);
                         const stats = await fs.stat(itemPath);

                         if (stats.isDirectory()) {
                              return {
                                   name: item,
                                   path: item,
                                   modified: stats.mtime,
                                   size: await this.getDirectorySize(itemPath)
                              };
                         }
                         return null;
                    })
               );

               return projects.filter(Boolean);
          } catch (error) {
               throw new Error(`Failed to list projects: ${error.message}`);
          }
     }

     // Get directory size recursively
     async getDirectorySize(dirPath) {
          try {
               const items = await fs.readdir(dirPath);
               let totalSize = 0;

               for (const item of items) {
                    const itemPath = path.join(dirPath, item);
                    const stats = await fs.stat(itemPath);

                    if (stats.isDirectory()) {
                         totalSize += await this.getDirectorySize(itemPath);
                    } else {
                         totalSize += stats.size;
                    }
               }

               return totalSize;
          } catch (error) {
               return 0;
          }
     }

     // Search files by content or name
     async searchFiles(basePath, searchTerm, searchInContent = false) {
          const results = [];

          const searchInDirectory = async (dirPath) => {
               try {
                    const items = await fs.readdir(dirPath);

                    for (const item of items) {
                         const itemPath = path.join(dirPath, item);
                         const stats = await fs.stat(itemPath);

                         if (stats.isDirectory()) {
                              await searchInDirectory(itemPath);
                         } else {
                              const relativePath = path.relative(basePath, itemPath);

                              // Search by filename
                              if (item.toLowerCase().includes(searchTerm.toLowerCase())) {
                                   results.push({
                                        path: relativePath,
                                        type: 'filename',
                                        match: item
                                   });
                              }

                              // Search by content (for text files under 1MB)
                              if (searchInContent && stats.size < 1024 * 1024) {
                                   try {
                                        const content = await fs.readFile(itemPath, 'utf8');
                                        if (content.toLowerCase().includes(searchTerm.toLowerCase())) {
                                             results.push({
                                                  path: relativePath,
                                                  type: 'content',
                                                  match: searchTerm
                                             });
                                        }
                                   } catch (error) {
                                        // Skip binary files
                                   }
                              }
                         }
                    }
               } catch (error) {
                    // Skip directories we can't access
               }
          };

          await searchInDirectory(basePath);
          return results;
     }
}

module.exports = new FileService(); 