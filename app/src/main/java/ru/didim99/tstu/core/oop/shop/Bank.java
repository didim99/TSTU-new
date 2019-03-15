package ru.didim99.tstu.core.oop.shop;

import java.util.Random;

/**
 * Created by didim99 on 13.03.19.
 */
public final class Bank {
  private static final int RND_FACTOR = 100;
  private static final int MIN_CLIENT_ID = 1000000000;
  private static final int MIN_SALARY = 20000;
  private static final int GOOD_SALARY = MIN_SALARY + MIN_SALARY / 2;
  private static final int START_MONEY = 50000;
  private static final int START_LIMIT = 20000;
  private static final int CREDIT_PERCENT = 12;

  private Random random;
  private long clientId;
  private int moneyValue;
  private int creditLimit;
  private int creditValue;

  Bank() {
    random = new Random();
    clientId = MIN_CLIENT_ID + random.nextInt(MIN_CLIENT_ID);
    moneyValue = START_MONEY + random.nextInt(
      START_MONEY / RND_FACTOR) * RND_FACTOR;
    creditLimit = START_LIMIT + random.nextInt(
      START_LIMIT / RND_FACTOR) * RND_FACTOR;
    creditValue = 0;
  }

  void purchase(int sum) {
    moneyValue -= sum;
  }

  public void takeCredit(int sum) {
    creditLimit -= sum;
    creditValue += sum;
    moneyValue += sum;
  }

  public void closeCredit() {
    creditLimit += moneyValue / (1 + random.nextInt(10));
    moneyValue -= creditValue;
    creditValue = 0;
  }

  public int getSalary() {
    int salary = MIN_SALARY + random.nextInt(
      MIN_SALARY / RND_FACTOR) * RND_FACTOR;
    if (creditValue > 0)
      creditValue += creditValue * CREDIT_PERCENT / 1200;
    moneyValue += salary;
    return salary;
  }

  public long getClientId() {
    return clientId;
  }

  public int getMoneyValue() {
    return moneyValue;
  }

  public int getCreditLimit() {
    return creditLimit;
  }

  public int getCreditValue() {
    return creditValue;
  }

  public boolean canTakeCredit() {
    return creditLimit > 0;
  }

  public boolean canCloseCredit() {
    return creditValue > 0 && moneyValue >= creditValue;
  }

  public static boolean isGoodSalary(int salary) {
    return salary >= GOOD_SALARY;
  }
}
