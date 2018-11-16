
//package singleseversim;
import java.util.Scanner;
import java.io.*;
import java.text.DecimalFormat;



public class SingleServerSim 
{
    //variable constants
     static final int Q_LIMIT = 1000; //Limit on the queue length
     static final int BUSY = 1; // Mnemonics for server being busy
     static final int IDLE = 0; //Mnemonics for server being idle
     
     static int next_event_type, num_custs_delayed, num_delays_required, num_events;
     static int num_in_q, server_status;
     static float area_num_in_q, area_server_status, mean_interarrival;
     static float mean_service, time, time_last_event, total_of_delays;
     
     //declaring the arrays
     static float[] time_arrival = new float[Q_LIMIT + 1];
     static float[] time_next_event = new float[3]; 
     
    //declaring the infile and outfile variables
     static Scanner infile;
     static PrintWriter outfile;
     
     static DecimalFormat df = new DecimalFormat("0.000");
     
     
     
     
     public static void main(String[] args) throws IOException
     {
         //open input and output files
         outfile = new PrintWriter("mm1.out");
         infile = new Scanner(new File ("mm1.in"));
         
         //specify the number of events for the timing function
         num_events = 2;
         
         //read input parameters
         mean_interarrival= Float.parseFloat(infile.nextLine());
         mean_service = Float.parseFloat(infile.nextLine());
         num_delays_required = Integer.parseInt(infile.nextLine());
         
         //Write report headings and input parameters
         outfile.println("Single Server Queueing System\n\n");
         outfile.println("Mean Interarrival Time "+df.format(mean_interarrival)+" minutes\n\n");
         outfile.println("Mean Service Time "+df.format(mean_service)+" minutes\n\n");
         outfile.println("Number of Customers "+num_delays_required+" \n\n");
         
         //initialize the simulation
         initialize();
         
         //Run the simulation while more delays are still needed
         while (num_custs_delayed < num_delays_required)
         {
             //determine the next event
             timing();
             
             //update time-avarage statisical accumulators
             update_time_avg_stats();
             
             //invoke appropriate event function
             switch(next_event_type)
             {
                 case 1:
                     arrive();
                     break;
                 case 2:
                     depart();
                     break;
             }
         }
         //invoke report generator 
         report();
         
         
        //closing the files and exiting
         outfile.close();
         infile.close();     
     }
     
     //Initialization function
     public static void initialize()
     {
         //Initialize the simulation clock
         time = (float)0.0;
         
         //initialize the state variables
         server_status = IDLE;
         num_in_q = 0;
         time_last_event = (float)0.0;
         
         //initialize the statistical counters
         num_custs_delayed = 0;
         total_of_delays = (float)0.0;
         area_num_in_q = (float)0.0;
         area_server_status = (float)0.0;
         
         //initialize next event
         //since no customers are present, the departure, is eliminated from consideration
         time_next_event[1] = time + expon(mean_interarrival);
         time_next_event[2] =(float)1.0e+30;
     }
     
     //Exponential Variable Generation Function
     public static float expon(float mean)
     {
         double random_num = Math.random();
         return -mean * (float)Math.log(random_num);
     }
     
     //Timing function
     public static void timing()
     {
         int i;
         float min_time_next_event = (float)1.0e+29;         
         next_event_type = 0;
         
         //Determine the event type of the next event to occur
         for(i=1; i<=num_events; ++i)
         {
             if(time_next_event[i] < min_time_next_event)
             {
                 min_time_next_event = time_next_event[i];
                 next_event_type = i;
             }
         }
         
         //Check to see whether the event list is empty
         if(next_event_type == 0)
         {
             //The event list is empty, so stop the simulation
             outfile.println("\nEvent List is Empty at time "+time);
             System.exit(1);
         }
         //the event list is not empty, so advance the simulation clock
         time = min_time_next_event;
     }
     
     //Arrival event function
     public static void arrive()
     {
         float delay;
         
         //Schedule next arrival
         time_next_event[1] = time + expon(mean_interarrival);
         
         //Check to see whether the server is busy;
         if(server_status == BUSY)
         {
             //Server is busy
             ++num_in_q;
             
             //Check to see whether an overflow condition exists
             if(num_in_q > Q_LIMIT)
             {
                 //The queue has overflowed, so stop the simulation
                 outfile.println("\nOverflow of the array time_arrival at "+time);
                 System.exit(2);
             }
             
             //There is still room in the queue, so store the time of arrival of the arriving customer
             //at the (new) end of time_arrival
             time_arrival[num_in_q] = time ;               
         }
         
         else
         {
             //Server is idle, so arriving Customer has delay of zero
             //the two lines do not affect the result of the simulation. they are for program clarity
             delay =(float) 0.0;
             total_of_delays +=delay ;
             
             //incrementing the number of customers delayed and make the server busy
             ++num_custs_delayed;
             server_status = BUSY;
             
             //Shedule a departure (service completion)
             time_next_event[2] = time + expon(mean_service);
         }
     }
     
     //Departure event function
     public static void depart()
     {
         int i;
         float delay;
         
         //Check to see whether the queue is empty
         if(num_in_q ==0)
         {
             //the queue is empty, so make the server idle and eliminate the departure(service completion)
             //event from consideration
             server_status = IDLE;
             time_next_event[2]=(float)1.0e+30;
         }
         else
         {
             //The queue is not empty
             //so decrement the number of customers in queue
             --num_in_q;
             
             //compute delay of the customer beginning service
             //update the total delay accumulator
             delay = time - time_arrival[1];
             total_of_delays += delay;
             
             //increment the number of customers delayed and schedule departure
             ++num_custs_delayed;
             time_next_event[2]= time + expon(mean_service);
             
             //move each customer in queue, if any, up one place;
             for (i=1; i<=num_in_q; ++i)
             {
                 time_arrival[i]=time_arrival[i+1];
             }             
         }
     }
     
     //update area accumulators for time_average statistics
     public static void update_time_avg_stats()
     {
         float time_since_last_event;
         
         //compute time since last event and update last-event marker
         time_since_last_event = time - time_last_event;
         time_last_event = time;
         
         //update area under number in queue function
         area_num_in_q += num_in_q * time_since_last_event;
         
         //update area under-server busy indicator function
         area_server_status += server_status * time_since_last_event;
     }
     
     //Report generator function
     public static void report()
     {
         //compute and write estimates of desired measures of performance
         outfile.println("\n\nAverage Delay in Queue "+df.format(total_of_delays/num_custs_delayed)+" minutes\n\n");
         outfile.println("Average  Number in Queue "+df.format(area_num_in_q/time)+"\n\n");
         outfile.println("Server Utilisation "+df.format(area_server_status/time)+" \n\n");
         outfile.println("Time simulation ended "+df.format(time)+" minutes");
     }
}
