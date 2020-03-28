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
    private static String OS = System.getProperty("os.name").toLowerCase();
    private static boolean isWindows() {
        return OS.contains("win");
    }
    private static String classPathDelimiter = isWindows() ? ";" : ":";

    //Globals for the main program
    private static ArrayList<Process> brokers = new ArrayList<>();
    private static ArrayList<Component> brokerAddr = new ArrayList<> ();

    private static ArrayList<Process> publishers = new ArrayList<>();



   private static Consumer c;

    private static int port = 5000;
    private static String ip = "127.0.0.1";
    private static String fileName = "brokers.txt";

    public static void startBrokers(int nBrokers){
        for (int i = 0 ; i < nBrokers ; i++){
            //Calculating hash of the broker
            int brokerHash = Utilities.getMd5(ip + port).hashCode();
            //Writing to brokers.txt
            String brokerLine = String.format("%s %d %d" , ip , port , brokerHash);
            appendToFile(brokerLine);
            //Starting the brokers
            String command = String.format("java Broker %s %d %s" , ip , port , fileName);
            try {
                Process p = Runtime.getRuntime().exec(command);
                brokers.add(p);
                //Keeping information about the current broker
                brokerAddr.add(new Component(ip,port));
                System.out.println("[RUNBROKERS] EXEC : " + command);
                //Next node must be started on a new port
                //Running gobblers for the created process' stderr , stdout
                StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream() , true);
                StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream() , false);
                errorGobbler.start();
                outputGobbler.start();
                port++;
            }
            catch (IOException e){
                System.out.println("[RUNBROKERS] Couldn't start a broker");
            }

        }
    }
    public static void startPublisher(String first , String last){
        //Starting a publisher
        String command = String.format("java -cp ." + classPathDelimiter + "mp3agic-0.9.0.jar Publisher %s %d %s %s %s%n" , ip , port , first ,last , fileName);
        try {
            //Starting the process
            Process p = Runtime.getRuntime().exec(command);
            publishers.add(p);
            System.out.println("[RUNBROKERS] EXEC : " + command);
            //Running gobblers for the created process' stderr , stdout
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream() , true);
            StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream() , false);
            errorGobbler.start();
            outputGobbler.start();
            port++;
            //System.out.println("[RUNBROKERS] EXEC : " + command);
        }
        catch (IOException e){
            System.out.println("[RUNBROKERS] Couldn't start a broker");
        }
    }

    private  static  void printHelp(){
        System.out.println("--------------------");
        System.out.print("exit : exit the program\n" +
                "start_publisher <first> <last> : start a publisher responsible for the range of names first-last\n" +
                "broker_status : show brokers and the artists they are responsible for\n" +
                "start_broker <n> : starts n new brokers at new ports\n"
        );
        System.out.println("--------------------");
    }

    /**
     * Creates an empty file
     */
    public static void initializeFile(){
        PrintWriter pw = null;
        try {
            File brokersFile = new File(fileName);
            pw = new PrintWriter(brokersFile);
        }
        catch(Exception e){
        }
        finally {
            if(pw != null) pw.close();
        }
    }
    public static void appendToFile(String line){
        try(FileWriter fw = new FileWriter(fileName, true);
            PrintWriter out = new PrintWriter(fw))
        {
            out.println(line);
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
    }

    public static void main(String[] args) throws IOException {

        initializeFile();
        //Array list containing the processes created
        Scanner sc = new Scanner(System.in);
        //Program loop watining for commands
        while(true) {
            String line = sc.nextLine();
            //If user typed exit break the loop and terminate created proesses
            String[] params = line.split(" ");
            try {
                if (line.trim().toLowerCase().equals("exit")) {
                    System.out.println("Bye");
                    break;
                } else if (line.trim().toLowerCase().startsWith("start_broker")) {
                    startBrokers(Integer.parseInt(params[1]));
                } else if (line.trim().toLowerCase().startsWith("start_publisher")) {
                    startPublisher(params[1], params[2]);
                }
                //Display status of brokers
                else if (line.trim().toLowerCase().startsWith("broker_status")) {
                    for (Component c : brokerAddr) {
                        System.out.printf("Broker %s %d status : %s%n", c.getIp(), c.getPort(), brokerStatus(c));
                    }
                }
                //TODO
                else if (line.trim().toLowerCase().startsWith("start_consumer")) {
                    //c = new Consumer();
                    //c.readBrokers("brokers.txt");
                } else if (line.trim().toLowerCase().startsWith("stream")) {
                    // if a consumer has started
                    // stream(artistname , songname)

                } else if (line.trim().toLowerCase().startsWith("download")) {
                    // if a consumer has started
                    // download(artistname , songname , outPutfilename)
                } else {
                    printHelp();
                }
            }
            //Something went wrong so we show the help menu to the user
            catch (Exception e){
                e.printStackTrace();
                printHelp();
            }
        }

        //Destroying the created processes and exiting after user input
        for(Process p : brokers){
            System.out.println("[RUNBROKERS] terminating a broker");
            p.destroy();
        }
        for(Process p : publishers){
            System.out.println("[RUNBROKERS] terminating a publisher");
            p.destroy();
        }
        System.out.println("[RUNBROKERS] finished stuff");
        //Exiting
        Runtime.getRuntime().exit(0);


    }
    private static boolean consumerHasStarted(){
        return c != null;
    }
    public static String brokerStatus(Component broker){
        String result = "";
        Request.ReplyFromBroker reply = null;
        Socket s = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            s = new Socket(broker.getIp() , broker.getPort());
            System.out.printf("[RUNBROKERS] Broker(%s,%d) connectionAvailable: %b %n" ,
                    broker.getIp() , broker.getPort() , s.isConnected());
            out =  new ObjectOutputStream(s.getOutputStream());
            //Creating request object
            Request.RequestToBroker request = new Request.RequestToBroker();
            request.method = Request.Methods.STATUS;
            //Sending request
            out.writeObject(request);
            in = new ObjectInputStream(s.getInputStream());
            reply = (Request.ReplyFromBroker) in.readObject();
            result = reply.artists.toString();

        }
        catch(Exception e){
            result = "Unreachable exception : " + e.getMessage();
        }
        finally{
            System.out.printf("[RUNBROKERS] closing connection with Broker(%s,%d)%n" ,
                    broker.getIp() , broker.getPort());
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
        //Indicates if the gobler handles an error stream
        boolean err;
        // reads everything from is until empty.
        StreamGobbler(InputStream is , boolean err) {
            this.is = is;
            this.err= err;
        }

        public void run() {
            Scanner sc = new Scanner(is);
            String line = null;
            try {
                while ((line = sc.nextLine()) != null) {
                    if(!err) {
                        System.out.println(line);
                    }
                    else{
                        System.err.println(line);
                    }
                }
            }
            //Sometimes no such element exception occurs when a process is destroyed
            catch(Exception e){

                System.out.println("Stream gobbler " + this + " terminating");
            }
        }
    }
}