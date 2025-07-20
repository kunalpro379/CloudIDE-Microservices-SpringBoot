C:\Users\kunal>docker run -d --rm --name mysqltest -e MYSQL_ROOT_PASSWORD=rootpassword -e MYSQL_DATABASE=mydb -e MYSQL_USER=kunal -e MYSQL_PASSWORD=kunal -p 3306:3306 mysql:8.0
939607c35cb15b96ce8144585bfd969b41cbf9aa01777500ab4d3cbfffae6a44

C:\Users\kunal>docker exec -it mysqltest mysql -u root -prootpassword -e "SELECT User, Host FROM mysql.user;"
mysql: [Warning] Using a password on the command line interface can be insecure.
+------------------+-----------+
| User             | Host      |
+------------------+-----------+
| kunal            | %         |
| root             | %         |
| mysql.infoschema | localhost |
| mysql.session    | localhost |
| mysql.sys        | localhost |
| root             | localhost |
+------------------+-----------+

C:\Users\kunal>


E:\Most_Important_Data\CloudCodeEditor\Backend\APIGateway>docker run -d --name mongodb-container -e MONGO_INITDB_DATABASE=cloudcodeeditor -p 27017:27017 -v mongodb_data:/data/db mongo:6.0
5b4bc7fe8d7ae1f02b729d09cfb7a9e526de6a08a83c021a8bd1bb081d3a6d8b

E:\Most_Important_Data\CloudCodeEditor\Backend\APIGateway>docker run -d --name redis-container -p 6379:6379 -v redis_data:/data redis:7-alpine
00b9a9b33944ab3426f9828a725e4fa36ff4b985bd779ac2e8ba15cf68eefc1e

E:\Most_Important_Data\CloudCodeEditor\Backend\APIGateway>