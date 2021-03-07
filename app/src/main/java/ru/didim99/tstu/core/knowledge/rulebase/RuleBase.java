package ru.didim99.tstu.core.knowledge.rulebase;

import android.os.AsyncTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 01.03.21.
 */

public class RuleBase {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_RuleBase";
  public static final String FILE_MASK = ".json";

  public enum Direction { FORWARD, BACKWARD }
  public enum SearchEvent { START, QUESTION, STEP, FINISH }

  private final List<Variable> variables;
  private final Map<String, Variable> varsIndex;
  private final List<Rule> rules;

  private List<Variable> facts;
  private Variable targetVariable;
  private Direction direction;
  private int reviewCount;

  private Finder finder;
  private EventListener listener;
  private Queue<Variable> questionQueue;
  private CountDownLatch userLock;

  public RuleBase(RuleBaseConfig config) {
    this.facts = new ArrayList<>();
    this.variables = config.getVariables();
    this.rules = config.getRules();
    this.varsIndex = new HashMap<>(variables.size());
    this.direction = Direction.FORWARD;
    this.questionQueue = new LinkedList<>();

    for (Variable var : variables) {
      varsIndex.put(var.getName(), var);
      if (var.getType() == Variable.Type.ENUM)
        var.setValues(config.getEnumValues(var.getName()));
    }

    String defaultTarget = config.getDefaultTarget();
    if (defaultTarget != null)
      targetVariable = varsIndex.get(defaultTarget);
    else targetVariable = variables.get(0);
  }

  public List<Rule> getRules() {
    return rules;
  }

  public List<Variable> getFacts() {
    return facts;
  }

  public int getReviewCount() {
    return reviewCount;
  }

  public int getTargetVarIndex() {
    return variables.indexOf(targetVariable);
  }

  public String[] getVarNames() {
    String[] names = new String[variables.size()];
    for (int i = 0; i < names.length; i++)
      names[i] = variables.get(i).getName();
    return names;
  }

  public Variable enqueueRequest() {
    Variable question = questionQueue.poll();
    if (question == null) userLock.countDown();
    return question;
  }

  public void setTargetVariable(int index) {
    this.targetVariable = variables.get(index);
  }

  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  public void addFact(Variable fact) {
    fact.setUserDefined(true);
    facts.add(fact);
  }

  public void startSearch(EventListener listener) {
    resetState();
    this.listener = listener;
    finder = new Finder(this);
    finder.execute();
  }

  private void resetState() {
    reviewCount = 0;
    facts.clear();

    for (Rule rule : rules)
      rule.reset();
  }

  private void findForward() throws InterruptedException {
    boolean done = false;
    while (!done) {
      MyLog.d(LOG_TAG, "Scanning base: " + reviewCount);
      reviewCount++;
      questionQueue.clear();

      for (Rule rule : rules) {
        if (rule.isAlreadyChecked())
          continue;

        Rule.Status status = rule.check(facts);
        MyLog.d(LOG_TAG, "Checked: " + rule + ", status=" + status);

        if (status == Rule.Status.FALSE)
          continue;

        if (status == Rule.Status.TRUE) {
          for (RuleTarget rt : rule.getTarget()) {
            Variable fact = new Variable(
              varsIndex.get(rt.getVariable()));
            fact.setValue(rt.getValue());
            facts.add(fact);
          }

          questionQueue.clear();
          break;
        }

        if (questionQueue.isEmpty()) {
          for (String name : rule.getUnknown())
            questionQueue.add(new Variable(varsIndex.get(name)));
        }
      }

      for (Variable fact : facts) {
        if (fact.equals(targetVariable)) {
          MyLog.d(LOG_TAG, "Target reached");
          questionQueue.clear();
          done = true;
          break;
        }
      }

      if (!questionQueue.isEmpty()) {
        MyLog.d(LOG_TAG, "Waiting for user answers: " + questionQueue.size());
        userLock = new CountDownLatch(1);
        finder.publishEvent(SearchEvent.QUESTION);
        userLock.await();
      }

      finder.publishEvent(SearchEvent.STEP);
    }
  }

  private static class Finder extends AsyncTask<Void, SearchEvent, Void> {

    private final RuleBase base;

    private Finder(RuleBase base) {
      this.base = base;
    }

    @Override
    protected void onPreExecute() {
      MyLog.d(LOG_TAG, "Finder execution started");
      if (base.listener != null)
        base.listener.onSearchEvent(SearchEvent.START);
    }

    @Override
    protected Void doInBackground(Void... voids) {

      try {
        MyLog.d(LOG_TAG, "Search started: " +
          (base.direction == Direction.FORWARD ? "forward" : "backward"));
        if (base.direction == Direction.FORWARD)
          base.findForward();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      return null;
    }

    @Override
    protected void onProgressUpdate(SearchEvent... events) {
      MyLog.d(LOG_TAG, "Publishing search event: " + events[0]);
      if (base.listener != null)
        base.listener.onSearchEvent(events[0]);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      MyLog.d(LOG_TAG, "Finder execution completed");
      if (base.listener != null)
        base.listener.onSearchEvent(SearchEvent.FINISH);
    }

    private void publishEvent(SearchEvent event) {
      publishProgress(event);
    }
  }

  public interface EventListener {
    void onSearchEvent(SearchEvent event);
  }
}
