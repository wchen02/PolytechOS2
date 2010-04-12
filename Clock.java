
import java.util.*;
import java.util.ArrayList;
import java.io.*;

public class Clock {

    private static boolean DEBUG = false; // for sterling algorithm
    private boolean DEBUG_OUT = false,// for internal use
            reevaluate = true;
    private int missPenalty = 0, quantum = 0,
            dirtyPagePenalty = 0, pageSize = 0,
            vaBits = 0, paBits = 0, frameCount = 0,
            numOfProcesses = 0, clockPointer = 0;
    private String referenceFile;
    private ArrayList<Process> processList = new ArrayList<Process>();
    private Process running = null;
    private boolean[] bitmap;
    private ArrayList<Page> clockStruct;

    public static void main(String... arg) {
        Clock clock = new Clock();
        clock.run();
    }

    public Clock() {
        readSettings("MemoryManagement.txt");
        // frameCount = 2^(paBits - pagesize bit)
        frameCount = (int) Math.pow(2,
                paBits - Math.log10(pageSize) / Math.log10(2)); // change of log base
        bitmap = new boolean[frameCount];
        clockStruct = new ArrayList<Page>(frameCount);
        for (int i = 0; i < bitmap.length; ++i) {
            bitmap[i] = false; //false is free
        }
        readReference(referenceFile);
    }

    public void run() {
        System.out.println();
        int memoryCycle = 1;

        System.out.println("References file: " + referenceFile
                + "\nPage size: " + pageSize
                + "\nVA size: " + vaBits
                + "\nPA size: " + paBits
                + "\nMiss penalty: " + missPenalty
                + "\nDirty page penalty: " + dirtyPagePenalty
                + "\nQuantum: " + quantum
                + "\nDebug: " + DEBUG
                + "\nFrame Count: "
                + frameCount + "\n" + "\nRunning Clock\n========");

        while (processList.size() > 0) {
            int VA, PA, frameNumber, pageNumber, offset;
            boolean nextProcess = false;
            int infinity = 0;
            while (!checkIfReady(processList.get(0))) {
                /* set top process to the next process and
                push the top to the end of the queue */
                Process top = processList.get(0);
                processList.remove(0); // Take O(N)
                processList.add(top);
                ++infinity;
                if (infinity >= processList.size()) {
                    top.pcb.decPenaltyTime();
                }
            } // if there's penalty on all processes this will generate an infinite loop

            running = processList.get(0);
            processList.remove(0); 

            System.out.println("Running " + running.pcb.getPid());
            int tmpBurst = running.pcb.getBurst();

            while (tmpBurst > 0) {
                --tmpBurst;
                decPenaltyForAll();
                if (running.pcb.topRef() != null) {
                    if (DEBUG) {
                        System.out.print("Clock: ");
                        for (int i = 0; i < clockStruct.size(); ++i) {
                            System.out.print(((clockPointer == i) ? "*" : "")
                                    + clockStruct.get(i).getFrameNumber()
                                    + ((clockStruct.get(i).referenced) ? "R" : "") + " ");
                        }
                        System.out.print("Free frames: ");
                        for (int i = 0; i < bitmap.length; ++i) {
                            System.out.print((!bitmap[i]) ? i + " " : "");
                        }
                        System.out.println();
                    }

                    VA = running.pcb.topRef().getAddress();
                    pageNumber = running.pcb.topRef().getAddress() / pageSize;
                    offset = running.pcb.topRef().getAddress() % pageSize;
                    System.out.print("R/W: " + ((running.pcb.topRef().getReadOrWrite()) ? "R" : "W")
                            + "; VA: " + VA
                            + "; Page: " + pageNumber 
                            + "; Offset: " + offset + "; ");

                    if (checkRefIfValid(running)) {
                        System.out.print("Hit; ");
                        // set referenced bit on
                        running.pt.getPageAtIndex(pageNumber).referenced = true;
                        int indexOfPage = clockStruct.indexOf(running.pt.getPageAtIndex(pageNumber));
                        Page clocktmpPage = null;
                        if (indexOfPage >= 0) {
                            clocktmpPage = clockStruct.get(indexOfPage);
                            clocktmpPage.referenced = true;
                            Reference runningtmpRef = running.pcb.topRef();

                            //read = true; --> meaning the reference was to read
                            if (!runningtmpRef.getReadOrWrite()) {
                                clocktmpPage.dirty = true;
                                running.pt.getPageAtIndex(pageNumber).dirty = true;
                            }
                            running.pcb.popRef();
                        } else {
                            System.out.println("error occured, the page of running.topRef does not match anything in the clockStruct, run() method clock check!");
                        }
                    } else {
                        running.pcb.setPenaltyTime(missPenalty);
                        clockAlg(running);
                        // re-evaluate
                        if (!reevaluate) {
                            running.pcb.popRef();
                        }
                        nextProcess = true;
                    }

                    frameNumber = running.pt.getPageAtIndex(pageNumber).getFrameNumber();
                    PA = frameNumber * pageSize + offset;
                    System.out.print("Frame: " + frameNumber + "; PA: " + PA);
                    System.out.println();
                    memoryCycle++;

                    if (DEBUG && running.pcb.getPenaltyTime() > 0) {
                        System.out.println("Process: " + running.pcb.getPid()
                                + " waiting: " + running.pcb.getPenaltyTime());
                    }
                }
                if(DEBUG){
                    for (Process process : processList) {
                        if (process.pcb.getPenaltyTime() > 0) {
                            System.out.println("Process: " + process.pcb.getPid() + " waiting: " + process.pcb.getPenaltyTime());
                        }
                    }
                }
                if (nextProcess) {
                    break;
                }
            }
            if (running.pcb.topRef() != null) {
                // move to the back of the queue
                processList.add(running);
            }

        }
    }

    private void decPenaltyForAll() {
        for (int i = 0; i < processList.size(); ++i) {
            Process tmp = processList.get(i);
            tmp.pcb.decPenaltyTime(); // nvr goes below 0
            processList.set(i, tmp);
        }

    }

    private void readReference(String filename) {
        try {
            Scanner scan = new Scanner(new File(filename));

            if (scan.hasNextLine()) {
                numOfProcesses = scan.nextInt();
            } else {
                System.out.println("Error: Nothing to read. Reference File.\n");
            }

            if (DEBUG_OUT) {
                System.out.print("\nin readReference()");
            }
            for (int i = 0; i < numOfProcesses; ++i) {
                if (scan.hasNextLine()) {
                    int tmpPid, tmpNumOfRefs;
                    tmpPid = scan.nextInt(); // pid
                    //tmpBurst = scan.nextInt(); // burst
                    tmpNumOfRefs = scan.nextInt();  // number of references
                    if (DEBUG_OUT) {
                        System.out.println("\n\tpid: " + tmpPid
                                + " ref#: " + tmpNumOfRefs);
                    }
                    scan.nextLine(); // gobble up the rest of the line

                    ArrayList<Reference> refs = new ArrayList<Reference>();
                    while (scan.hasNextLine()) {
                        String line = scan.nextLine();
                        if (line.isEmpty()) {
                            break;
                        }

                        Scanner scanLine = new Scanner(line);
                        Reference reference = new Reference(scanLine.nextInt(), // address
                                (scanLine.hasNext("R")) // read or write
                                );
                        if (DEBUG_OUT) {
                            System.out.println("\tVA: " + reference.getAddress()
                                    + " R/W: " + ((reference.getReadOrWrite()) ? "R" : "W"));
                        }
                        refs.add(reference);
                    }
                    processList.add(new Process(new PageTable(bitmap.length),
                            new ProcessControlBlock(tmpPid, quantum, tmpNumOfRefs, refs)));
                }
            }
        } catch (java.io.FileNotFoundException e) {
            System.out.println("Exception in readReference(), in clock class ");
            e.printStackTrace();
        }
    }

    /* reads the setting file */
    private void readSettings(String filename) {
        try {
            Scanner scan = new Scanner(new File(filename));
            String line, value, arg;
            if (DEBUG_OUT) {
                System.out.println("in readSetting()");
            }
            while (scan.hasNextLine()) {
                line = scan.nextLine();
                // find the equal sign and 
                // left of the sign is the argument
                // right of the sign is the value
                int indexEquals = line.indexOf("=");
                arg = line.substring(0, indexEquals);
                value = line.substring(indexEquals + 1);
                if (DEBUG_OUT) {
                    System.out.println("\t" + arg + " = " + value);
                }
                setValue(arg, value);
            }
        } catch (java.io.FileNotFoundException e) {
            System.out.println("Exception in readSetting()");
            e.printStackTrace();
        }
    }

    /* set value to specific arguments */
    private void setValue(String arg, String value) {
        if (arg.toLowerCase().equals("misspenalty")) {
            missPenalty = Integer.valueOf(value);
        } else if (arg.toLowerCase().equals("debug")) {
            DEBUG = Boolean.valueOf(value);
        } else if (arg.toLowerCase().equals("referencefile")) {
            referenceFile = value;
        } else if (arg.toLowerCase().equals("dirtypagepenalty")) {
            dirtyPagePenalty = Integer.valueOf(value);
        } else if (arg.toLowerCase().equals("pagesize")) {
            pageSize = Integer.valueOf(value);
        } else if (arg.toLowerCase().equals("vabits")) {
            vaBits = Integer.valueOf(value);
        } else if (arg.toLowerCase().equals("pabits")) {
            paBits = Integer.valueOf(value);
        } else if (arg.toLowerCase().equals("quantum")) {
            quantum = Integer.valueOf(value);
        } else if (arg.toLowerCase().equals("reevaluate")) {
            reevaluate = Boolean.valueOf(value);
        } else {
            System.out.println("Error: Argument not found! "
                    + "\nArgument:" + arg
                    + "\nValue: " + value);
        }
    }

    public boolean checkIfReady(Process process) { // true if no penalty
        return (process.pcb.getPenaltyTime() == 0);
    }

    public boolean checkRefIfValid(Process running) {
        int VA = running.pcb.topRef().getAddress();
        int index = VA / pageSize;

        return running.pt.getPageAtIndex(index).valid;
    }

    /* Clock page replacement algorithm */
    void clockAlg(Process process) {
        Reference topRef = process.pcb.topRef();
        int pageNumber = topRef.getAddress() / pageSize;

        /* if there exist free frames, use the one with lowest frame number */
        for (int i = 0; i < frameCount; ++i) {
            if (!bitmap[i]) { // if free
                Page newFrame = new Page(i);
                newFrame.valid = true;
                newFrame.referenced = true;
                bitmap[i] = true; // it is now occupy
                process.pt.setPageAtIndex(pageNumber, newFrame);

                if (clockStruct.size() > 0) {
                    // adds to the before the clockptr
                    clockStruct.add(clockPointer, newFrame); 
                    clockPointer = (clockPointer + 1) % bitmap.length;
                } else {
                    clockStruct.add(newFrame);
                }
                System.out.print("Free; ");
                return;
            }
        }

        /* moves the clock pointer,
         if the ref bit is on turn it off and advances the pointer */
        for (int i = 0; i < clockStruct.size(); ++i) {
            if (clockStruct.get(clockPointer).referenced) {
                // clear ref bit and advance
                clockStruct.get(clockPointer).referenced = false;
                // advances the clockPtr
                clockPointer = (clockPointer + 1) % bitmap.length;
            } else {
                break; // breaks out if the ref bit is off
            }
        }

        // if the clock pointer has move one full cycle, replaces the first page
        if (clockStruct.get(clockPointer).dirty) { // Dirty
            process.pcb.setPenaltyTime(dirtyPagePenalty +
                    process.pcb.getPenaltyTime());
            System.out.print("Dirty; ");
        } else {
            System.out.print("Clean; ");
        }
        //replaces page, turn the valid bit of the current frame to be replaced off
        //the replacing page, turn the ref and valid bit on and advances the clock pointer
        clockStruct.get(clockPointer).valid = false;
        int frameNumber = clockStruct.get(clockPointer).getFrameNumber();
        process.pt.getPageAtIndex(pageNumber).valid = true;
        process.pt.getPageAtIndex(pageNumber).referenced = true;
        process.pt.getPageAtIndex(pageNumber).setFrameNumber(frameNumber);
        clockStruct.set(clockPointer, process.pt.getPageAtIndex(pageNumber));
        clockPointer = (clockPointer + 1) % bitmap.length;
    }
}
