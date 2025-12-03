package com.compiler;

public class Timer {
  private long startTime;
  private long endTime;
  private boolean running = false;

  public void Start() {
    running = true;
    startTime = System.nanoTime();
  }

  public void Stop() {
    if (running) {
      endTime = System.nanoTime();
      running = false;
    }
  }

  public long GetElapsedNanoSeconds() {
    if (running) {
      return System.nanoTime() - startTime;
    }
    return endTime - startTime;
  }

  public double GetElapsedMicroSeconds() {
    return GetElapsedNanoSeconds() / 1_000.0;
  }

  public double GetElapsedMilliSeconds() {
    return GetElapsedNanoSeconds() / 1_000_000.0;
  }

  public double GetElapsedSeconds() {
    return GetElapsedNanoSeconds() / 1_000_000_000.0;
  }
}

