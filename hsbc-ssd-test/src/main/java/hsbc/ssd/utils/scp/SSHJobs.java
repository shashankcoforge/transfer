package hsbc.ssd.utils.scp;

import com.jcraft.jsch.*;
import hsbc.ssd.utils.helper.PropertyHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Properties;

public class SSHJobs {
    static Logger log = LogManager.getLogger(SSHJobs.class);

    public static void main(String[] arg) throws IOException, JSchException {

        String user = "abc";
        String host = "localhost";
        int port = 22;

        String password = "";

        Session session = createSession(user, host, port, password);
        runCommandOnRemote(session, "File Mover");
    }

    public static Session createSession(String user, String host, int port, String password) {
        try {
            JSch jsch = new JSch();

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");

            Session session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig(config);
            session.connect();

            return session;
        } catch (JSchException e) {
            System.out.println(e);
            return null;
        }
    }

    public static void runCommandOnRemote(Session session, String jobName) throws JSchException, IOException {

        String command = PropertyHelper.getEnvSpecificAppParameters(jobName);
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);

        channel.setInputStream(null);
        ((ChannelExec)channel).setErrStream(System.err);

        InputStream in=channel.getInputStream();

        channel.connect();

        byte[] tmp=new byte[1024];
        while(true){
            while(in.available()>0){
                int i=in.read(tmp, 0, 1024);
                if(i<0)break;
                System.out.print(new String(tmp, 0, i));
            }
            if(channel.isClosed()){
                if(in.available()>0) continue;
                System.out.println("exit-status: "+channel.getExitStatus());
                break;
            }
            try{Thread.sleep(1000);}catch(Exception ee){}
        }
        channel.disconnect();
        session.disconnect();
    }
}
