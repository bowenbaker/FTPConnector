import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * Created by jasonzhang on 6/12/14.
 */

import org.apache.commons.net.ftp.FTPSClient;
import java.util.ArrayList;


public class FTPSConnector extends ClientModel {

    private FTPSClient ftps;


    /**
     * This constructor is for implicit FTPS and you don't have to specify a protocol.
     * @param host hostname
     * @param user username
     * @param pwd password
     * @param bufferSize 0 standard
     */
    public FTPSConnector(String host, String user, String pwd,long bufferSize){
        setParams(host, port, user, pwd);
        isConnected = initilizeConnection();
        if(isConnected) {
            try {
                ftps.execPBSZ(bufferSize);
            }
            catch(Exception e){
                System.out.println("Failed to initilize FTPES parameters");
            }
        }
    }

    /**
     *This constructor is for explicit FTPS, and you must specify a protocol
     *
     * @param host hostname
     * @param port port number : 21 for explicit, 990 for implicit
     * @param user username
     * @param pwd password
     * @param protocol "SSL" or "TLS"
     * @param bufferSize 0 is standard
     * @param prot protection, "P" is standard but :
     *             "C" : Clear
     *             "S" : Safe (SSL Protocol only)
     *             "E" : Confidential (SSL Protocol only)
     *             "P" : Private
     */
    public FTPSConnector(String host, int port, String user, String pwd, long bufferSize, String prot, String protocol){
        setParams(host, port, user, pwd);
        ftps = new FTPSClient(protocol);
        isConnected = initilizeConnection();
        if(isConnected) {
            try {
                ftps.execAUTH(protocol);
                ftps.execPBSZ(bufferSize);
                ftps.execPROT(prot);

            }
            catch(Exception e){
                System.out.println("Failed to initilize FTPES parameters");
            }
        }
    }

    private void setParams(String host, int port, String user, String pwd){
        hostname = host;
        port = port;
        username = user;
        password = pwd;
    }

    @Override
    public void rename(String oldFileName, String newFileName) throws IOException {
        ftps.rename(oldFileName,newFileName);
    }

    @Override
    public String[] ls(String path, boolean includeFiles, boolean includeDirectories) throws IOException {
        ArrayList<String> list = new ArrayList<String>();
        if(includeFiles)
            for(FTPFile file : ftps.listFiles())
                list.add(file.getName());
        if(includeDirectories)
            for(FTPFile dir : ftps.listDirectories())
                list.add(dir.getName());
        return (String[]) list.toArray();
    }

    @Override
    public void mkdir(String directoryName) throws IOException {
        ftps.makeDirectory(workingDirectory + directoryName);
    }

    @Override
    public void rmdir(String directoryName) throws IOException {
        ftps.removeDirectory(workingDirectory + directoryName);
    }

    @Override
    public void rm(String fileName) throws IOException {
        ftps.deleteFile(workingDirectory + fileName);
    }

    @Override
    public void disconnect() {
        if (this.ftps.isConnected()) {
            try {
                this.ftps.logout();
                this.ftps.disconnect();
            } catch (IOException f) {
                System.out.println("Failed to disconnect");
                f.printStackTrace();
            }
        }
    }

    @Override
    public void upload(String localFileFullName, String desiredDestinationFileName) {
        try{
            InputStream input = new FileInputStream(new File(localFileFullName));
            this.ftps.storeFile(workingDirectory + desiredDestinationFileName, input);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void download(String fileName, String localFilePath) {
        try {
            FileOutputStream fos = new FileOutputStream(localFilePath);
            this.ftps.retrieveFile(workingDirectory + fileName, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean initilizeConnection() {
        ftps.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        int reply;
        try {
            ftps.connect(hostname, port);
            reply = ftps.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftps.disconnect();
            }
            ftps.login(username, password);
            ftps.setFileType(FTP.BINARY_FILE_TYPE); //TODO different file types?
            ftps.enterLocalPassiveMode();
            return true;
        }
        catch(Exception e){
            return false;
        }
    }


    /**
     * Change directory inside FTP Server
     * @param newDirectory must end in "/"
     */
    @Override
    public void changeWorkingDirectory(String newDirectory) {
        workingDirectory = newDirectory;

    }
}
