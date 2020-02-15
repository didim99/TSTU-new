package ru.didim99.tstu.core.oop.abiturient;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 23.02.19.
 */
public class AbiturientManager {
  private ArrayList<Abiturient> abiturients;

  public AbiturientManager() {
    abiturients = new ArrayList<>();
  }

  public void add(Abiturient a) {
    abiturients.add(a);
  }

  public Abiturient get(int id) {
    for (Abiturient a : abiturients)
      if (a.getId() == id) return a;
    return null;
  }

  public void remove(int id) {
    for (Abiturient a : abiturients) {
      if (a.getId() == id) {
        abiturients.remove(a);
        break;
      }
    }
  }

  public ArrayList<Abiturient> getAll() {
    return abiturients;
  }

  public ArrayList<Abiturient> getBadGrades() {
    ArrayList<Abiturient> res = new ArrayList<>();
    for (Abiturient a : abiturients)
      if (a.getGrades().hasBad()) res.add(a);
    return res;
  }

  public ArrayList<Abiturient> getForSum(int sum) {
    ArrayList<Abiturient> res = new ArrayList<>();
    for (Abiturient a : abiturients)
      if (a.getGrades().getSum() >= sum) res.add(a);
    return res;
  }

  public ArrayList<Abiturient> getTop(int count) {
    ArrayList<Abiturient> res = new ArrayList<>(abiturients);
    Collections.sort(res, (o1, o2) ->
      Integer.compare(o2.getGrades().getSum(), o1.getGrades().getSum()));
    res.subList(count, res.size()).clear();
    return res;
  }

  public ArrayList<Abiturient> getHalfPass(int sum) {
    int targetSum = sum - 1;
    ArrayList<Abiturient> res = new ArrayList<>();
    for (Abiturient a : abiturients)
      if (a.getGrades().getSum() == targetSum) res.add(a);
    return res;
  }

  public void save(String path) throws IOException {
    ArrayList<String> data = new ArrayList<>();
    Gson packer = new Gson();
    for (Abiturient a : abiturients)
      data.add(packer.toJson(a));
    Utils.writeFile(path, data);
  }

  public void load(String path) throws IOException {
    ArrayList<String> data = Utils.readFile(path);
    Gson parser = new Gson();
    abiturients.clear();
    for (String str : data)
      abiturients.add(parser.fromJson(str, Abiturient.class).init());
  }
}
