import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class HTTPConnection extends Thread{
    private Socket socket;
    Date startTime = new Date();


    //constructor
    public HTTPConnection(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            request();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    //
    private void request() throws Exception, ParseException {

        //read input and output stream
        BufferedReader inbr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        DataOutputStream outData = new DataOutputStream(socket.getOutputStream());

        //get first line of request message
        String requestMessageLine = inbr.readLine();

        //print
        System.out.println();
        System.out.println(requestMessageLine);

        //for header line and if modified since handling
        String headerLine;
        String ifModifiedSinceLine = null;
        Date modiSinceDate = null;

        //print header line and check if it contains if modified since
        //if 'If-Modified-Since' exists, store it into string for further process
        while ( (headerLine = inbr.readLine()).length() != 0 ) {
            System.out.println(headerLine);
            if (headerLine.contains("If-Modified-Since")) {
                ifModifiedSinceLine = headerLine;
            }
        }

        //date variable of if modified since for comparison with last modified for 304 response
        if (ifModifiedSinceLine != null) {
            String sinceDate = ifModifiedSinceLine.substring(19);
            SimpleDateFormat sFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
            modiSinceDate = sFormat.parse(sinceDate);
        }

        //tokenize the request message line to get file name
        StringTokenizer tokenizedLine = new StringTokenizer(requestMessageLine);

        //to indicate get, head and other type of method
        boolean getTrue = false;
        boolean headTrue = false;
        if (tokenizedLine.nextToken().equals("GET")) {
            getTrue = true;
        } else if (tokenizedLine.nextToken().equals("GET")) {
            headTrue = true;
        }

        //get filename, which comes after method
        String fileName = "";
        fileName = tokenizedLine.nextToken();
        if (fileName.startsWith("/") == true) {
            fileName = fileName.substring(1);
        }

        //open file
        File file = new File(fileName);

        //get last modified date and change to acceptable format
        long lastModified = file.lastModified();
        SimpleDateFormat newFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        newFormat.setTimeZone(TimeZone.getTimeZone("GMT")); //change timezone to GMT
        Date lastModifiedDate = new Date(lastModified);
        String newDate = newFormat.format(lastModifiedDate);

        //if compDate = 0 : both dates are equal (304 Not Modified)
        //if compDate > 0 : If Modified Since Date is earlier than last Modified Date (200 OK)
        //if compDate < 0 : If Modified Since Date is after the last Modified Date (304 Not Modified)
        int compDate = 0;
        boolean notModified = false;
        if (ifModifiedSinceLine != null) {
            compDate = modiSinceDate.compareTo(lastModifiedDate);
            if (compDate == 0 || compDate <0 ) {
                notModified = true;
            }
        }

        //process for file
        int numOfBytes = (int)file.length();
        FileInputStream inFile = null;

        //to check if file exists or not
        //needed for handling 404 response
        boolean fileExists = true;
        try {
            inFile = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            fileExists = false;
        }
        byte[] fileInBytes = new byte[numOfBytes];

        //Strings for writing to outputstream later
        String response = "";
        String contentType = "";
        String body = "";
        String responseType = "";   //for log file

        //for GET and HEAD method
        //when file exists in the current directory
        if (fileExists) {
            if (getTrue == true || headTrue == true) {

                // if 'If-Modified-Since' exists but file modified
                // if no 'If-Modified-Since'
                // 200 OK situation
                if (ifModifiedSinceLine == null || notModified == false) {

                    //set contentType to corresponding type
                    if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
                        contentType = "Content-type: image/jpeg\r\n";
                    else if (fileName.endsWith(".gif"))
                        contentType = "Content-type: image/gif\r\n";
                    else if (fileName.endsWith(".html"))
                        contentType = "Content-type: text/html\r\n";

                    //response line construction - CRLF added at the end of the each line
                    response = "HTTP/1.0 200 OK\r\n" + //status line + CRLF
                                "Last-Modified: " + newDate + "\r\n" + //last modified + CRLF
                                contentType +   //CRLF already added
                                "Content-Length: " + numOfBytes + "\r\n";
                    responseType = "200 OK";    //for log file
                } else { // not modified file after such date
                    body = "<html><head><title>304 Not Modified</title></head><body>304 Not Modified</body></html>";
                    contentType = "Content-type: text/html\r\n";
                    response = "HTTP/1.0 304 Not Modified\r\n" +
                            contentType;
                    responseType = "304 Not Modified";
                }

            } else { //in case requested method is other than 'GET' or 'HEAD'
                body = "<html><head><title>400 Bad Request</title></head><body>400 Bad Request</body></html>";
                contentType = "Content-type: text/html\r\n";
                response = "HTTP/1.0 400 Bad Request\r\n" +
                        contentType;
                responseType = "400 Bad Request";
            }

        } else {    // when there is no file found in the current directory
            body = "<html><head><title>404 Not Found</title></head><body>404 Not Found</body></html>";
            contentType = "Content-type: text/html\r\n";
            response = "HTTP/1.0 404 Not Found\r\n" +
                    contentType;
            responseType = "404 Not Found";

        }

        //write the response message
        outData.writeBytes(response);
        outData.writeBytes("\r\n");

        //only GET method copies the content to the body
        //HEAD method does not need to copy the content
        if (fileExists && getTrue == true) {
            int bytes = 0;
            while ((bytes = inFile.read(fileInBytes)) != -1) {
                outData.write(fileInBytes, 0, bytes);
            }
            inFile.close();
        } else outData.writeBytes(body); // for 400 and 404, copy html code in the body

        //for hostname in log file
        InetAddress ip;
        String hostName = "";
        ip = InetAddress.getLocalHost();

        //if logfile.txt exists in the file it appends the log line by line
        FileWriter logFile = new FileWriter("logfile.txt", true);

        Date endTime = new Date();
        //write the log into the file
        logFile.write(ip + " " + startTime.toString() + " " + endTime.toString() + " "
                            + fileName + " " + responseType +"\n");
        logFile.close();

        outData.close();
        inbr.close();
        socket.close();
    }

}
