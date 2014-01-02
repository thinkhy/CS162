package nachos.threads;

import java.util.LinkedList; // +hy+
import java.util.Iterator;   // +hy+

import nachos.ag.BoatGrader;
import nachos.machine.*;  // for Lib.assertTrue()
    
public class Boat
{
    static BoatGrader bg;
                    
    // define two location
    static final int Oahu = 0;
    static final int Molokai = 1;

    static int boatLocation = Oahu;        // where is the boat
    static int cntPassengers = 0; 
                            
    static Lock boatLock = new Lock();     // boat holds a lock
    static Condition2 waitingOnOahu    = new Condition2(boatLock);
    static Condition2 waitingOnMolokai = new Condition2(boatLock);
     
    static int OahuChildren = 0;
    static int OahuAdults = 0;
    static int MolokaiChildren = 0;
    static int MolokaiAdults = 0;
     
    static Communicator reporter = new Communicator();
     
    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();
	 
	System.out.println("\n***Testing Boats with only 2 children***");

	// begin(0, 2, b);
    
	begin(1, 2, b);

    /*
	begin(2, 2, b);
	begin(3, 2, b);
	begin(4, 2, b);

	begin(1, 3, b);
	begin(2, 3, b);
	begin(3, 3, b);
	begin(4, 3, b);
    */
     
    // System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
    // 	 begin(1, 2, b);
     
    // System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
    //   begin(3, 3, b);
    
    }
     
    public static void begin( int adults, int children, BoatGrader b)
    {

	// Store the externally generated autograder in a class
	// variable to be accessible by children.
	bg = b;


    // Initialize counters
    OahuChildren = children;
    OahuAdults = adults;
    MolokaiChildren = 0;
    MolokaiAdults = 0;

	// Instantiate global variables here
	
	// Create threads here. See section 3.4 of the Nachos for Java
	// Walkthrough linked from the projects page.
    Runnable r_child = new Runnable() {

        public void run() {
            int location = Oahu;  // thread local varialbe, indicate where person is
            ChildItinerary(location);
        };
    };

    Runnable r_adult = new Runnable() {

        public void run() {
            int location = Oahu;  // thread local varialbe, indicate where person is
            AdultItinerary(location);
        };
    };
     
    for (int i = 0; i < children; i++) {
        KThread t = new KThread(r_child);
        t.setName("Boat Thread - Child - #" + (i+1));

        t.fork();
    }
     
    for (int i = 0; i < adults; i++) {
        KThread t = new KThread(r_adult);
        t.setName("Boat Thread - Adult - #" + (i+1));
        t.fork();
    }
        
    while(true) 
    {
        int recv = reporter.listen();

        System.out.println("***** Receive " + recv);

        if (recv == children + adults)
        {
            break;
        }
    }
        
    }

    static void ChildItinerary(int location)
    {
       System.out.println("***** ChildItinerary, place: " + location);
        /* This is where you should put your solutions. Make calls
           to the BoatGrader to show that it is synchronized. For
           example:
               bg.AdultRowToMolokai();
           indicates that an adult has rowed the boat across to Molokai
        */

       System.out.println("cntPassengers:" + cntPassengers + "\n");

       boatLock.acquire(); 

       while (true) {
            if (location == Oahu)
            {
               // wait until boat's arrival
               while (boatLocation != Oahu) 
               {
                   waitingOnOahu.sleep();
               }

               waitingOnOahu.wakeAll();

               // if nobody is in Oahu, Child will return to Molokai
               if (OahuChildren == 0 && OahuAdults == 0) 
               {  
                   bg.ChildRideToMolokai();

                   boatLocation = Oahu;
                   MolokaiChildren++;
                   location = Molokai;

                   System.out.println("\n***[Game Over]***");
                   break;
               }

               // wait until available seat on boat
               while (cntPassengers >= 2) 
               { 
                   waitingOnOahu.sleep();
               }

               // book the seat on boat
               cntPassengers++;

               System.out.println("cntPassengers:"+cntPassengers+"\n");

               // two children on boat, the second children ride to Molokai
               if (cntPassengers == 2) 
               {  
                    // clear passenger number
                    cntPassengers = 0;

                    bg.ChildRideToMolokai();
                    OahuChildren--;

                    boatLocation = Molokai;
                    MolokaiChildren++;

                    location = Molokai; 
                    reporter.speak(MolokaiChildren+MolokaiAdults);

                    // children arrive in Molokai, wake up all persons on Molokai
                    waitingOnMolokai.wakeAll();

                    // current child is sleeping
                    waitingOnMolokai.sleep();
               }
               // the first passenger(pilot) rows to Molokai
               else if (cntPassengers == 1) 
               {      

                    bg.ChildRowToMolokai();
                    OahuChildren--;
                    location = Molokai; 
                    MolokaiChildren++;

                    // no child left in Oahu, only send one child to Molokai
                    if (OahuChildren == 0) 
                    {
                        // clear passenger number
                        cntPassengers = 0;

                        boatLocation = Molokai;
                        reporter.speak(MolokaiChildren+MolokaiAdults);

                        // Children arrive in Molokai, wake up all persons on Molokai
                        waitingOnMolokai.wakeAll();
                    }

                    // current child is sleeping
                    waitingOnMolokai.sleep();
               }
            }
            else if (location == Molokai) 
            {
               Lib.assertTrue(MolokaiChildren > 0);

               while (boatLocation != Molokai) 
               {
                   waitingOnMolokai.sleep();
               }

               waitingOnMolokai.wakeAll();

               // note, just need one child pilot back to Oahu
               MolokaiChildren--;
               bg.ChildRowToOahu();

               boatLocation = Oahu;
               location = Oahu; 
               OahuChildren++;

               waitingOnOahu.wakeAll();
               waitingOnOahu.sleep();
               
            }

        } // while (1);

        boatLock.release(); 
    }


    static void AdultItinerary(int location)
    {
       boatLock.acquire(); 

       while (true)
       {
           if (location == Oahu)
           {
               // child first, then send adults to Molokai
               // but leave one child in Oahu
               while (cntPassengers > 0 || OahuChildren > 1 || boatLocation != Oahu) 
               {
                   waitingOnOahu.sleep();
               }

               bg.AdultRowToMolokai();
               OahuAdults--;

               boatLocation = Molokai;
               MolokaiAdults++;

               location = Molokai; 
               reporter.speak(MolokaiChildren+MolokaiAdults);

               // adult arrive in Molokai, wake up all persons on Molokai
               waitingOnMolokai.wakeAll();

               // current adult is sleeping
               waitingOnMolokai.sleep();
           }
           else if (location == Molokai)
           {
               // Do nothing
               waitingOnMolokai.sleep();
           }
           else 
           {
               break;
           }
       }

       boatLock.release(); 
    }

    static void SampleItinerary()
    {
        // Please note that this isn't a valid solution (you can't fit
        // all of them on the boat). Please also note that you may not
        // have a single thread calculate a solution and then just play
        // it back at the autograder -- you will be caught.
        System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
        bg.AdultRowToMolokai();
        bg.ChildRideToMolokai();
        bg.AdultRideToMolokai();
        bg.ChildRideToMolokai();
    }

}


