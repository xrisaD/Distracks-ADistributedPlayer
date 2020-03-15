import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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

        //Array list containing the brokers
        ArrayList<Process> brokers = new ArrayList<>();
        for (int i = 0 ; i < nBrokers ; i++){
            //Calculating hash of the broker
            int brokerHash = Utilities.getMd5(ip + port).hashCode();
            //Writing to brokers.txt
            pw.printf("%s %d %d%n" , ip , port , brokerHash);
            //Starting the brokers

            String command = String.format("java Broker %s %d %s" , ip , port , fileName);
            Process p = Runtime.getRuntime().exec(command);
            //Keeping information about the current broker
            brokers.add(p);
            System.out.println("[RUNBROKERS] EXEC : " + command);
            //Next node must be started on a new port
            port++;
        }
        pw.close();

        Scanner sc = new Scanner(System.in);
        while(true) {
            String line = sc.nextLine();
            //If user typed exit break the loop and terminate created proesses
            if(line.trim().toLowerCase().equals("exit")){
                System.out.println("Bye");
                break;
            }
            else{
                System.out.println("Type exit to exit");
            }
        }

        //Destroying the created processes and exiting after user input
        for(Process p : brokers){
            p.destroy();
        }


    }
}
