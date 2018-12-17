package controller.peer.server;

import model.FileInfo;

import java.io.*;
import java.net.Socket;

class Sender extends Thread {

    private String sharedDirectory;
    private Socket socket;
    private String filename;

    Sender(Socket socket, String FileDir) {
        this.socket = socket;
        this.sharedDirectory = FileDir;
    }

    public void run() {

        InputStream is = null;
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        OutputStream out = null;
        FileInfo fileInfo = null;

        try {
            is = socket.getInputStream();            //Connecting controller.client.Client acting as a controller.server to the file requesting controller.client.Client
            ois = new ObjectInputStream(is);
            fileInfo = (FileInfo) ois.readObject();                    //Filename to be downloaded
            filename = fileInfo.getFileName();
        } catch (Exception ex){
            System.out.println("Can't receive filename. ");
        }

        try {
            System.out.println("Preparing to send " + filename);
            File file = new File(sharedDirectory + "//" + filename);
            // Get the size of the file
            out = socket.getOutputStream();
            oos = new ObjectOutputStream(out);

            //only set part of in bytes
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            int mySize = (int) file.length() / fileInfo.getTotalNumberOfPeers();
            if(mySize < 0)
                mySize = (int) (0xffffffffl + mySize);
            byte[] buffer = new byte[mySize];
            try {
                raf.seek(fileInfo.getMyPart() * mySize);
                raf.readFully(buffer, 0, mySize);
            } finally {
                raf.close();
            }


            fileInfo.setFileData(buffer);

            System.out.println("\nSending... ");

            oos.flush();
            oos.writeObject(fileInfo);

            out.close();
            socket.close();
            ois.close();
            oos.close();
            is.close();

            System.out.println("File was successfully sent. ");

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
