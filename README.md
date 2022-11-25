# HTTP


How to compile and run the program

1) 	place HTTP.java and HTTPConnection.java into same directory

2) 	then compile HTTP.java file with the command:
    ```
    javac HTTP.java
    ```

3)	it will show two .class files 

4) 	then, start the program with the command:
	  ```
    java HTTP
    ```

5)	open the browser and type 127.0.0.1:5600
	if it is successfully connected, the browser will show 404 Not Found page

6)	create any file such as hello.html , hello.jpg within the same directory

7)	then, type 127.0.0.1:5600/hello.html OR 127.0.0.1:5600/hello.jpg
	the browser will display the corresponding file. 

8)  terminal will display the request message the server gets

 Notes) 	
  Whever the program starts, logfile.txt will be created for the log of the each request.
  However, it will be created newly everytime it restarts the program.
