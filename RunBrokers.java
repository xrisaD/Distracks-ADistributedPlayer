import com.sun.source.tree.Scope;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
//hashing imports
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RunBrokers {
    /**
     * Starts brokers locally with different ports
     *              Usage RunBrokers <nBrokers>
     */
    public static void main(String[] args) throws IOException {
        //Usage RunBrokers <nBrokers>
        int nBrokers = Integer.parseInt(args[0]);
        String ip = "127.0.0.1";
        int port = 5000;
        String fileName = "brokers.txt";


        File brokersFile =  new File(fileName);
        PrintWriter pw = new PrintWriter(brokersFile);

        //Array list containing the processes created
        ArrayList<Process> brokers = new ArrayList<>();
        ArrayList<Component> brokerAddr = new ArrayList<>();

        ArrayList<Process> publishers = new ArrayList<>();


        ArrayList<Process> consumers = new ArrayList<>();


        for (int i = 0 ; i < nBrokers ; i++){
            //Calculating hash of the broker
            int brokerHash = Utilities.getMd5(ip + port).hashCode();
            //Writing to brokers.txt
            pw.printf("%s %d %d%n" , ip , port , brokerHash);
            //Starting the brokers
            String command = String.format("java Broker %s %d %d %s" , ip , port , brokerHash , brokersFile);
            Process p = Runtime.getRuntime().exec(command);
            //Keeping information about the current broker
            brokers.add(p);
            brokerAddr.add(new Component(ip,port));
            System.out.println("[RUNBROKERS] EXEC : " + command);
            //Next node must be started on a new port
            //Running gobblers for the created process' stderr , stdout
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream());
            StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream());
            errorGobbler.start();
            outputGobbler.start();
            port++;
        }
        pw.close();

        Scanner sc = new Scanner(System.in);
        //Program loop watining for commands
        while(true) {
            String line = sc.nextLine();
            //If user typed exit break the loop and terminate created proesses
            if(line.trim().toLowerCase().equals("exit")){
                System.out.println("Bye");
                break;
            }
            else if(line.trim().toLowerCase().equals("start_publisher")) {
                //Starting a publisher
                String command = String.format("java Publisher %s %d %s" , ip , port , fileName);
                //Adding artists for whom the publisher is responsible to the command
                String[] artists = {"Bob" , "John" , "mpla"};
                for (String artist : artists ){
                    command += " " + artist;
                }
                //Starting the process
                Process p = Runtime.getRuntime().exec(command);
                System.out.println("[RUNBROKERS] EXEC : " + command);
                //Running gobblers for the created process' stderr , stdout
                StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream());
                StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream());
                errorGobbler.start();
                outputGobbler.start();
                port++;
                //System.out.println("[RUNBROKERS] EXEC : " + command);
            }
            //Display status of brokers
            else if(line.trim().toLowerCase().equals("broker_status")){
                for(Component c : brokerAddr){
                    System.out.printf("Broker %s %d status : %s%n" , c.getIp() , c.getPort() , brokerStatus(c));
                }
            }
            else{
                System.out.print("exit : exit the program\n" +
                        "start_publisher : start a publisher\n" +
                        "status : show brokers and the artists they are responsible for\n"
                );
            }
        }

        //Destroying the created processes and exiting after user input
        for(Process p : brokers){
            p.destroy();
        }
        for(Process p : consumers){
            p.destroy();
        }
        for(Process p : publishers){
            p.destroy();
        }


    }
    public static String brokerStatus(Component broker){
        String result = "";
        Socket s = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            s = new Socket(broker.getIp() , broker.getPort());
            out =  new ObjectOutputStream(s.getOutputStream());
            String query = "status";
            out.writeObject(query);
            in = new ObjectInputStream(s.getInputStream());
            result = (String) in.readObject();
            result = "NumArtists: " + result.split(" ").length + " " + result;

        }
        catch(Exception e){
            result = "Unreachable";
        }
        finally{
            try {
                if(in != null) in.close();
                if(out != null) out.close();
                if(s != null)  s.close();
            }
            catch(Exception e){
                throw new Error();
            }
        }
        return result;
    }

    static class StreamGobbler extends Thread {
        InputStream is;

        // reads everything from is until empty.
        StreamGobbler(InputStream is) {
            this.is = is;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line=null;
                while ( (line = br.readLine()) != null)
                    System.out.println(line);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
