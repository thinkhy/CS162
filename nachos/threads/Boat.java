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
    static Condition2 waitingOnOahu     = new Condition2(boatLock);
    static Condition2 waitingOnMolokai  = new Condition2(boatLock);
    static Condition2 waitingOnBoatFull = new Condition2(boatLock);
    
     
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
    
    // Var1: just one child 
    // expected result: OK
	// begin(0, 1, b);

    // Var2: two child 
    // expected result: OK
	// begin(0, 2, b);

    // Var3: three child 
    // expected result: OK
	// begin(0, 3, b);

    // Var4: one adult
    // expected result: Failed
	// begin(1, 0, b);
    //
    // Var5: one adult, one child
    // expected result: Failed
	// begin(1, 1, b);

    // Var6: one adult, two child
    // expected result: OK
	// begin(1, 2, b);
    
    // Var7: one adult, three child
    // expected result: OK
	// begin(1, 3, b);
    
    // Var8: two adult, two child
    // expected result: OK
	// begin(2, 2, b);
    //
    // Var9: two adult, two child
    // expected result: OK
	// begin(3, 2, b);

    // Var10: lots of adult, two child
    // expected result: OK
	// begin(10, 2, b);

    // Var11: lots of adult, lots of child
    // expected result: OK
	// begin(10, 20, b);

    // Var12: stress testing
    // expected result: OK
	begin(100, 50, b);

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

            if (location == 123)
            {
               // unreachable path
               Lib.assertTrue(false);
               break; // ploace a break to cheat JAVA compiler
            }

            if (location == Oahu)
            {

               // wait until boat's arrival and available seat on boat
               // if only one child left in Oahu, adults go first
               while (boatLocation != Oahu || cntPassengers >= 2
                       || (OahuAdults > 0 && OahuChildren == 1) ) 
               {
                   waitingOnOahu.sleep();
               }

               waitingOnOahu.wakeAll();
                
               // if no adult and only one child left in Oahu, the child row to Molokai directly 
               if (OahuAdults == 0 && OahuChildren == 1) 
               {
                   OahuChildren--;
                   bg.ChildRowToMolokai();

                   boatLocation = Molokai;
                   location = Molokai; 
                   MolokaiChildren++;

                   // clear passenger number after arrival
                   cntPassengers = 0;

                   // collate the number of people in Molokai
                   reporter.speak(MolokaiChildren+MolokaiAdults);

                   // child arrives in Molokai, to wake up one person in Molokai
                   waitingOnMolokai.wakeAll();
                    
                   // current child is sleeping in Molokai
                   waitingOnMolokai.sleep();
                    
               }
               else if (OahuChildren > 1) // send children to Molokai first
               {

                   // book the seat on boat
                   cntPassengers++;

                   // two children on boat, the second child rides to Molokai
                   if (cntPassengers == 2) 
                   {  

                        // notify the fisrt guy to row to Molokai
                        waitingOnBoatFull.wake();

                        waitingOnBoatFull.sleep();

                        // then ride myself to Molokai
                        OahuChildren--;
                        bg.ChildRideToMolokai();

                        // all the children get off boat, decrease passenger number
                        cntPassengers = cntPassengers - 2;

                        // note, now boat arrives on Molokai
                        boatLocation = Molokai;

                        location = Molokai; 

                        MolokaiChildren++;

                        reporter.speak(MolokaiChildren+MolokaiAdults);

                        // two children arrive in Molokai, wake up one child in Molokai
                        waitingOnMolokai.wakeAll();

                        // current child is sleeping
                        waitingOnMolokai.sleep();
                   }
                   // the first passenger(pilot) rows to Molokai
                   else if (cntPassengers == 1) 
                   {      
                        // only one child on board, wait for next child(passenger)  comming
                        waitingOnBoatFull.sleep();
                        
                        OahuChildren--;
                        
                        bg.ChildRowToMolokai();

                        location = Molokai; 
                        MolokaiChildren++;
                        
                        // notify another passenger on baord to leave
                        waitingOnBoatFull.wake();

                        // current child is sleeping
                        waitingOnMolokai.sleep();
                   }
               } // if OahuChildren > 1
            }
            else if (location == Molokai) 
            {
               Lib.assertTrue(MolokaiChildren > 0);

               while (boatLocation != Molokai) 
               {
                   waitingOnMolokai.sleep();
               }

               // note, just need one child pilot back to Oahu
               MolokaiChildren--;
               bg.ChildRowToOahu();

               boatLocation = Oahu;
               location = Oahu; 
               OahuChildren++;

               waitingOnOahu.wakeAll();
               waitingOnOahu.sleep();
            }

        } // while (true)

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
               while (cntPassengers > 0 
                       || OahuChildren > 1 || boatLocation != Oahu) 
               {
                   waitingOnOahu.sleep();
               }

               bg.AdultRowToMolokai();
               OahuAdults--;

               boatLocation = Molokai;
               MolokaiAdults++;

               location = Molokai; 
               reporter.speak(MolokaiChildren+MolokaiAdults);

               Lib.assertTrue(MolokaiChildren > 0);

               // adult arrive in Molokai, wake up one child in Molokai
               waitingOnMolokai.wakeAll();

               // current adult is sleeping
               waitingOnMolokai.sleep();
           }
           else if (location == Molokai)
           {
               waitingOnMolokai.sleep();
           }
           else 
           {
               // unreachable path
               Lib.assertTrue(false);
               break; // ploace a break to cheat JAVA compiler
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


