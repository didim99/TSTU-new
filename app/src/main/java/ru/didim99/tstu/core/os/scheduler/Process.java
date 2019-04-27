package ru.didim99.tstu.core.os.scheduler;

import com.google.gson.annotations.SerializedName;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 23.04.19.
 */
public class Process {
  private static final AtomicInteger idSource = new AtomicInteger();
  private static final int TICKS_RESIDENT = -1;
  public static final int MAX_VIEW_TICKS = 200;

  private static final int INT_TMIN = 50000;
  private static final int INT_TMAX = 200000;
  private static final int INT_RMIN = 50000;
  private static final int INT_RMAX = 200000;
  private static final int MIN_EAX = 0x00000000;
  private static final int MAX_EAX = 0x00004000;
  private static final int MIN_EBX = 0x0004D000;
  private static final int MAX_EBX = 0x0006F000;
  private static final int MIN_ECX = 0x00000000;
  private static final int MAX_ECX = 0x0000FFFF;
  private static final int MIN_EDX = 0x007F0000;
  private static final int MAX_EDX = 0x00FFFFFF;
  private static final int MIN_ESP = 0x7FF00000;
  private static final int MAX_ESP = 0x7FFF0000;
  private static final int MIN_EBP = 0x7FF20000;
  private static final int MAX_EBP = 0x7FFFFFFF;
  private static final int MIN_CS = 0x00040000;
  private static final int MAX_CS = 0x00080000;
  private static final int MIN_DS = 0x00F40000;
  private static final int MAX_DS = 0x00F80000;

  public static final class Type {
    public static final int REGULAR   = 0;
    public static final int RESIDENT  = 1;
    public static final int INTERRUPT = 2;
  }

  // State
  private int type;
  private final int id;
  private int usedTicks;
  private int dynamicRAM;
  private Info info;
  // Hardware
  private int[] registers;
  private int mmuWait;
  private int ioDelay, ioWait;
  private MemoryManager mmu;

  Process(Info info, MemoryManager mmu) {
    this.registers = new int[Processor.REG_COUNT];
    this.id = idSource.getAndIncrement();
    this.type = info.totalTicks == TICKS_RESIDENT
      ? Type.RESIDENT : Type.REGULAR;
    this.info = new Info(info);
    this.mmu = mmu;
  }

  Process(String name, Random random, MemoryManager mmu) {
    int ticks = Utils.randInRange(random, INT_TMIN, INT_TMAX);
    int bRAM = Utils.randInRange(random, INT_RMIN, INT_RMAX);
    this.info = new Info(name, ticks, bRAM, 0, 0);
    this.registers = new int[Processor.REG_COUNT];
    this.type = Type.INTERRUPT;
    this.mmu = mmu;
    this.id = -1;
  }

  void init(Random random) {
    registers[Processor.CS] = Utils.randInRange(random, MIN_CS, MAX_CS);
    registers[Processor.DS] = Utils.randInRange(random, MIN_DS, MAX_DS);
    registers[Processor.ESP] = Utils.randInRange(random, MIN_ESP, MAX_ESP);
    registers[Processor.EBP] = Utils.randInRange(random, MIN_EBP, MAX_EBP);
    registers[Processor.EAX] = Utils.randInRange(random, MIN_EAX, MAX_EAX);
    registers[Processor.EBX] = Utils.randInRange(random, MIN_EBX, MAX_EBX);
    registers[Processor.ECX] = Utils.randInRange(random, MIN_ECX, MAX_ECX);
    registers[Processor.EDX] = Utils.randInRange(random, MIN_EDX, MAX_EDX);
    ioDelay = info.ioRate > 0 ? random.nextInt(info.ioRate) : TICKS_RESIDENT;
    ioWait = info.ioRate > 0 ? random.nextInt(info.ioRate) : TICKS_RESIDENT;
    mmu.alloc(info.baseRAM);
  }

  void onAttachedToCPU(Random random) {
    if (ioDelay > 0) ioDelay--;
    if (ioDelay == 0 && ioWait == 0) {
      ioDelay = random.nextInt(info.ioRate);
      ioWait = Utils.randInRange(random, 1, info.ioRate);
    }
  }

  void doTick(Random random, long ticks) {
    if (ioDelay == 0 && ioWait > 0) ioWait--;
    usedTicks += ticks;
    if (mmuWait-- == 0) {
      mmuWait = random.nextInt(5);
      int maxAlloc = (info.dRAMLimit - dynamicRAM) / 10;
      int maxFree = dynamicRAM / 5;
      if (random.nextBoolean() && maxAlloc > 0) {
        int alloc = random.nextInt(maxAlloc);
        if (mmu.alloc(alloc))
          dynamicRAM += alloc;
      } else if (maxFree > 0) {
        int free = random.nextInt(maxFree);
        dynamicRAM -= free;
        mmu.free(free);
      }
    }
  }

  void free() {
    mmu.free(info.baseRAM + dynamicRAM);
  }

  int[] registers() {
    return registers;
  }

  boolean isWaitingForIO() {
    if (info.ioRate == 0) return false;
    return ioDelay == 0 && ioWait == 0;
  }

  boolean isFinished() {
    if (type == Type.RESIDENT) return false;
    return usedTicks >= info.totalTicks;
  }

  public int getId() {
    return id;
  }

  public int getType() {
    return type;
  }

  public String getName() {
    return info.name;
  }

  public int getCPUTime() {
    return usedTicks;
  }

  public int getRamUsage() {
    return info.baseRAM + dynamicRAM;
  }

  public int getProgress() {
    if (info.totalTicks == TICKS_RESIDENT) return MAX_VIEW_TICKS;
    return usedTicks * MAX_VIEW_TICKS / info.totalTicks;
  }

  public double GetProgressPercent() {
    return usedTicks * 100.0 / info.totalTicks;
  }

  @Override
  public String toString() {
    return "Process " + id + " " + info;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Process)) return false;
    Process process = (Process) obj;
    return process.id == id;
  }

  static void resetCounter() {
    idSource.set(0);
  }

  public static class Info implements Cloneable {
    @SerializedName("name")
    private String name;
    @SerializedName("ticks")
    private int totalTicks;
    @SerializedName("bRAM")
    private int baseRAM;
    @SerializedName("dRAMLimit")
    private int dRAMLimit;
    @SerializedName("ioRate")
    private int ioRate;

    Info(Info src) {
      this(src.name, src.totalTicks, src.baseRAM,
        src.dRAMLimit, src.ioRate);
    }

    public Info(String name, Integer ticks, int bRAM,
                int dRAM, Integer ioRate) {
      this.name = name;
      this.baseRAM = bRAM;
      this.dRAMLimit = dRAM;
      this.totalTicks = ticks == null ? TICKS_RESIDENT : ticks;
      this.ioRate = ioRate == null ? 0 : ioRate;
    }

    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return "[" + name + "] "
        + totalTicks + "/" + baseRAM + "/"
        + dRAMLimit + "/" + ioRate;
    }
  }
}
