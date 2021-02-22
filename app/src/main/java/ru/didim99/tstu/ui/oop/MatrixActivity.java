package ru.didim99.tstu.ui.oop;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.math.common.Matrix;
import ru.didim99.tstu.core.oop.SquareMatrix;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.utils.InputValidator;
import ru.didim99.tstu.utils.InputValidator.ValidationException;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 23.02.19.
 */
public class MatrixActivity extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_MatrixAct";
  private static final int DEFAULT_MIN = -100;
  private static final int DEFAULT_MAX = 100;
  private static final int DEFAULT_SIZE = 4;

  private TextView tvSrc;
  private TextView tvOut;
  private EditText etSrc;
  private EditText etFactor;
  private Button btnAdd;
  private Button btnScale;
  private Button btnMultiply;
  private Button btnTranspose;
  private InputValidator iv;
  private boolean genSquare;
  private boolean operandA;
  private Matrix a, b, r;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "MatrixActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_oop_matrix);
    iv = InputValidator.getInstance();
    genSquare = false;
    operandA = true;

    MyLog.d(LOG_TAG, "View components init...");
    tvSrc = findViewById(R.id.tvSrc);
    tvOut = findViewById(R.id.tvOut);
    etSrc = findViewById(R.id.etSrc);
    etFactor = findViewById(R.id.etFactor);
    btnAdd = findViewById(R.id.btnAdd);
    btnScale = findViewById(R.id.btnMultiplyConst);
    btnMultiply = findViewById(R.id.btnMultiply);
    btnTranspose = findViewById(R.id.btnTranspose);

    findViewById(R.id.btnCreate)
      .setOnClickListener(v -> parseMatrix());
    findViewById(R.id.btnCreateRnd)
      .setOnClickListener(v -> generateRandom());
    btnAdd.setOnClickListener(v -> add());
    btnScale.setOnClickListener(v -> scale());
    btnMultiply.setOnClickListener(v -> multiply());
    btnTranspose.setOnClickListener(v -> transpose());
    ((CheckBox) findViewById(R.id.cbSquare))
      .setOnCheckedChangeListener((buttonView, isChecked)
        -> genSquare = isChecked);
    ((RadioGroup) findViewById(R.id.rgOperand))
      .setOnCheckedChangeListener((radioGroup, currVer) -> {
      switch (currVer) {
        case R.id.useAsA: operandA = true; break;
        case R.id.useAsB: operandA = false; break;
      }
    });

    MyLog.d(LOG_TAG, "MatrixActivity started");
  }

  private void parseMatrix() {
    try {
      String src = iv.checkEmptyStr(etSrc,
        R.string.errOOP_emptyMatrix, "Matrix data");
      MyLog.d(LOG_TAG, "Parsing matrix from input");

      double data[][] = null;
      String[] lines = src.split("\n");
      int rows = lines.length, columns = 0;
      int row = 0, column;
      for (String l : lines) {
        String[] digits = l.split("\\s+");
        if (columns == 0) {
          columns = digits.length;
          data = new double[rows][columns];
        }

        if (digits.length != columns)
          throw new NumberFormatException();
        column = 0;
        for (String s : digits)
          data[row][column++] = Double.parseDouble(s);
        row++;
      }

      String name = operandA ? "A" : "B";
      Matrix m = new Matrix(name, data);
      if (operandA) a = m;
      else b = m;
      updateUI();
    } catch (ValidationException ignored) {}
      catch (NumberFormatException e) {
        MyLog.w(LOG_TAG, "Incorrect data format");
        Toast.makeText(this, R.string.errOOP_incorrectMatrix,
          Toast.LENGTH_LONG).show();
    }
  }

  private void generateRandom() {
    MyLog.d(LOG_TAG, "Generating random matrix");
    String name = operandA ? "A" : "B";
    Matrix m;
    if (genSquare) {
      m = SquareMatrix.createRandom(name,
        DEFAULT_SIZE, DEFAULT_MIN, DEFAULT_MAX);
    } else {
      m = Matrix.createRandom(name, DEFAULT_SIZE,
        DEFAULT_SIZE - 1, DEFAULT_MIN, DEFAULT_MAX);
    }
    if (operandA) a = m;
    else b = m;
    updateUI();
  }

  private void scale() {
    try {
      double f = iv.checkFloat(etFactor, null, R.string.errOOP_emptyFactor,
        R.string.errOOP_incorrectFactor, "MultiplyFactor");
      MyLog.d(LOG_TAG, "Scaling matrix A to: " + f);
      r = a.multiply(f);
      updateUI();
    } catch (ValidationException ignored) {}
  }

  private void add() {
    MyLog.d(LOG_TAG, "Adding matrix A to B");
    r = a.add(b);
    updateUI();
  }

  private void multiply() {
    MyLog.d(LOG_TAG, "Multiplying matrix A to B");
    r = ((SquareMatrix) a)
      .multiply((SquareMatrix) b);
    updateUI();
  }

  private void transpose() {
    MyLog.d(LOG_TAG, "Transposing matrix A");
    r = ((SquareMatrix) a).transpose();
    updateUI();
  }

  private void updateUI() {
    MyLog.d(LOG_TAG, "Updating UI state");
    boolean hasA = a != null;
    boolean hasB = b != null;
    String src = "";

    if (hasA) src = a.toString().concat("\n\n");
    if (hasB) src = src.concat(b.toString());
    btnScale.setEnabled(hasA);
    btnAdd.setEnabled(hasA && hasB && a.sameAs(b));
    btnTranspose.setEnabled(a instanceof SquareMatrix);
    btnMultiply.setEnabled(a instanceof SquareMatrix
      && b instanceof SquareMatrix && a.sameAs(b));
    if (r != null) tvOut.setText(r.toString());
    else tvOut.setText(null);
    tvSrc.setText(src);
  }
}
