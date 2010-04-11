
import java.util.*;
import java.util.ArrayList;

import java.io.*;

public class Clock {

    private static boolean DEBUG = true; // for sterling algor.
    private boolean DEBUG_OUT = false,
            reevaluate = true;    // for internal use, comment to false
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
        frameCount = (int)Math.pow(2, paBits - Math.log10(pageSize)/Math.log10(2));
        bitmap = new boolean[frameCount];
        clockStruct = new ArrayList<Page>(frameCount);
        for (int i = 0; i < bitmap.length; ++i) {
            bitmap[i] = false; //false is free
        }
        readReference(referenceFile);
    }

    public void run() {
        System.err.println();
        int memoryCycle = 1;

        System.out.println("References file: " + referenceFile + "\nPage size: " + pageSize + "\nVA size: " + vaBits + "\nPA size: " + paBits + "\nMiss penalty: " + missPenalty + "\nDirty page penalty: " + dirtyPagePenalty + "\nDebug: " + DEBUG + "\nFrame Count: " + frameCount + "\n" + "\nRunning Clock\n========");

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
                    Process tmp = processList.get(0);
                    tmp.pcb.decPenaltyTime();
                    processList.set(0, tmp);
                }
            } // if there's penalty on all processes this will generate an infinite loop

            running = processList.get(0); // this has to change ! update Note to self
            processList.remove(0); // Take O(N)

            System.out.println("Running " + running.pcb.getPid());
            //if (checkRefIfValid(running.pcb.topRef())) {//**************************************************************************
            int tmpBurst = running.pcb.getBurst();

            while (tmpBurst > 0) {
                --tmpBurst;
                decPenaltyForAll();
                if (running.pcb.topRef() != null) {
                    if (DEBUG) {
                        System.out.print("Clock: ");
                        for (int i = 0; i < clockStruct.size(); ++i) {
                            System.out.print(( (clockPointer == i) ? "*" : "" ) + clockStruct.get(i).getFrameNumber() + ((clockStruct.get(i).referenced) ? "R" : "") + " ");
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
                    System.out.print("R/W: " + ((running.pcb.topRef().getReadOrWrite()) ? "R" : "W") + "; VA: " + VA + "; Page: " + pageNumber + "; Offset: " + offset + "; ");

                    if (checkRefIfValid(/*checks if Virtual Addres map is valid*/running/*.pcb.topRef()*/)) {
                        System.out.print("Hit; ");
                        // set referenced bit on
                        setBit(running, "referenced", true);
                        int indexOfPage = clockStruct.indexOf(running.pt.getPageAtIndex(pageNumber));
                        Page clocktmpPage = null;
                        if (indexOfPage >= 0) {
                            clocktmpPage = clockStruct.get(indexOfPage);
                            clocktmpPage.referenced = true;
                            Reference runningtmpRef = running.pcb.topRef();

                            //read = true; --> meaning the reference was to read
                            if (!runningtmpRef.getReadOrWrite()) {
                                clocktmpPage.dirty = true;
                                setBit(running, "dirty", true);
                            }
                            running.pcb.popRef();
                        }else
                            System.out.println("error occured, the page of running.topRef does not match anything in the clockStruct, run() method clock check!");
                    } else {
                        clockAlg(running);
                        // re-evaluate
                        nextProcess = true;
                    }

                    frameNumber = running.pt.getPageAtIndex(pageNumber).getFrameNumber();
                    PA = frameNumber * pageSize + offset;
                    System.out.print("Frame: " + frameNumber + "; PA: " + PA);

                    System.out.println();
                    memoryCycle++;

                    if(running.pcb.getPenaltyTime() > 0) {
                        System.out.println("Process: " + running.pcb.getPid() + " waiting: " + running.pcb.getPenaltyTime());
                    }
                    for(Process process : processList){
                        if(process.pcb.getPenaltyTime() > 0){
                            System.out.println("Process: " + process.pcb.getPid()
                                    + "waiting: " + process.pcb.getPenaltyTime());
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

    private void setBit(Process running, String type, boolean bool) {
        int VA = running.pcb.topRef().getAddress();
        int index = VA / pageSize;
        if (type.equals("valid")) {
            running.pt.getPageAtIndex(index).valid = bool;
        } else if (type.equals("referenced")) {
            running.pt.getPageAtIndex(index).referenced = bool;
        } else if (type.equals("dirty")) {
            running.pt.getPageAtIndex(index).dirty = bool;
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
        /*
         *# of processes
         * process list
         * pid
         * burst, Defined as the number of memory references
        between I/O requests.
         * # of references for the process
        list of references for the process.
        Each reference indicates whether it was a
        read or a write
         */
        try {
            Scanner scan = new Scanner(new File(filename));

            if (scan.hasNextLine()) {
                numOfProcesses = scan.nextInt();
            } else {
                System.out.println("Error: Nothing to read. Reference File.\n");
            }

            for (int i = 0; i < numOfProcesses; ++i) {
                if (scan.hasNextLine()) {
                    int tmpPid, tmpBurst, tmpNumOfRefs;
                    tmpPid = scan.nextInt(); // pid
                    tmpBurst = scan.nextInt(); // burst
                    tmpNumOfRefs = scan.nextInt();  // number of references
                    if (DEBUG_OUT) {
                        System.out.println("\npid: " + tmpPid +
                                "\nburst: " + tmpBurst +
                                "\nnum of refs: " + tmpNumOfRefs);
                    }
                    scan.nextLine(); // gobble up the rest of the line

                    ArrayList<Reference> refs = new ArrayList<Reference>();
                    while (scan.hasNextLine()) {
                        String line = scan.nextLine();
                        if (line.isEmpty()) {
                            break;
                        }
                        if (DEBUG_OUT) {
                            System.out.println("\nline: " + line +
                                    "\nsizeOfLine: " + line.length()); // output for debug

                        }
                        Scanner scanLine = new Scanner(line);
                        Reference reference = new Reference(scanLine.nextInt(), // address
                                (scanLine.hasNext("R")) // read or write
                                );
                        refs.add(reference);
                    }
                    processList.add(new Process(new PageTable(bitmap.length), new ProcessControlBlock(tmpPid, tmpBurst, tmpNumOfRefs, refs)));
                }
            }
        } catch (java.io.FileNotFoundException e) {
            System.out.println("Exception in readReference(), in clock class ");
            e.printStackTrace();
        }
    }

    private void readSettings(String filename) {
        /*
        referenceFile=references.txt
        quantum=4
        missPenalty=1
        dirtyPagePenalty=1
        pageSize=1024
        VAbits=16
        PAbits=13
        debug=true
        reevaluate=true
         */
        try {
            Scanner scan = new Scanner(new File(filename));
            String line, value, arg;
            while (scan.hasNextLine()) {
                line = scan.nextLine();
                int indexEquals = line.indexOf("=");
                arg = line.substring(0, indexEquals);
                value = line.substring(indexEquals + 1);
                if (DEBUG_OUT) {
                    System.out.println("in readSetting()\nargnument: " + arg + "\nvalue: " + value);
                }
                setValue(arg, value);
            }
        } catch (java.io.FileNotFoundException e) {
            System.out.println("Exception in readSetting(), in clock class ");
            e.printStackTrace();
        }
    }

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
        }else if (arg.toLowerCase().equals("reevaluate")) {
            reevaluate = Boolean.valueOf(value);
        }else {
            System.out.println("Error: Argument not found! \nArgument:" + arg + "\nValue: " + value);
        }
    }
	
    public boolean checkIfReady(Process process) { // true if no penalty
        return (process.pcb.getPenaltyTime() == 0);
    }

    public boolean checkRefIfValid(Process running) {
        int VA = running.pcb.topRef().getAddress();
        int index = VA / pageSize;

        if (DEBUG_OUT) {
            System.out.println("checking for out of bound index is:" + index + "\nVirtual Address: " + VA);

        }
        return running.pt.getPageAtIndex(index).valid;
    }

    void clockAlg(Process process) {
        Reference topRef = process.pcb.topRef();
        int pageNumber = topRef.getAddress() / pageSize;

        for (int i = 0; i < frameCount; ++i) {
            if (!bitmap[i]) { // if free
                Page newFrame = new Page(i);
                newFrame.valid = true;
                newFrame.referenced = true;
                bitmap[i] = true; // it is now occupy
                process.pt.setPageAtIndex(pageNumber, newFrame);
                process.pcb.setPenaltyTime(missPenalty);
                if(clockStruct.size() > 0){
                    clockStruct.add(clockPointer, newFrame); // adds to the before the clockptr
                    clockPointer = (clockPointer + 1)%bitmap.length;
                }else
                    clockStruct.add(newFrame);
                System.out.print("Free; ");
                return;
            }
        }

        for (int i = 0; i < clockStruct.size(); ++i) {
            if (clockStruct.get(0).referenced) { // means the page was referenced
                // clear ref bit and advance
                Page refPage = clockStruct.get(i);
                refPage.referenced = false;
                process.pt.setPageAtIndex(pageNumber, refPage);
                clockStruct.set(i, refPage);
                // advances the clockPtr
                clockPointer = (clockPointer + 1)%bitmap.length;
            } else {
                // replace with the page at index: page number of page table

                clockStruct.set(i, process.pt.getPageAtIndex(pageNumber));

                if (clockStruct.get(i).dirty) { // Dirty
                    process.pcb.setPenaltyTime(dirtyPagePenalty +
                            process.pcb.getPenaltyTime());
                    System.out.print("Dirty; ");
                } else {
                    System.out.print("Clean; ");
                }
                return;
            }
        }

        // the hand pointer has move one full cycle, replace the first page
        clockStruct.set(0, process.pt.getPageAtIndex(pageNumber));

        if (clockStruct.get(0).dirty) { // Dirty
            process.pcb.setPenaltyTime(dirtyPagePenalty +
                    process.pcb.getPenaltyTime());
            System.out.print("Dirty; ");
        } else {
            System.out.print("Clean; ");
        }
    }
}
