package ru.didim99.tstu.ui.mp.lab3;

import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import java.util.Calendar;
import java.util.GregorianCalendar;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.mp.notice.Notice;
import ru.didim99.tstu.core.mp.notice.NoticeManager;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.utils.InputValidator;
import ru.didim99.tstu.utils.InputValidator.ValidationException;

public class ActTask32 extends BaseActivity {

  private DatePicker datePicker;
  private TimePicker timePicker;
  private EditText etHeader;
  private EditText etText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_mp_l3_t3_p2);

    findViewById(R.id.mp_btnSave).
      setOnClickListener(v -> saveAndReturn());
    datePicker = findViewById(R.id.mp_date);
    timePicker = findViewById(R.id.mp_time);
    etHeader = findViewById(R.id.etHeader);
    etText = findViewById(R.id.etText);
    timePicker.setIs24HourView(true);
  }

  private void saveAndReturn() {
    try {
      InputValidator iv = InputValidator.getInstance();
      String header = iv.checkEmptyStr(etHeader,
        R.string.errNotice_emptyHeader, "Header");
      String text = iv.checkEmptyStr(etText,
        R.string.errNotice_emptyHeader, "Text");
      Calendar c = new GregorianCalendar(datePicker.getYear(),
        datePicker.getMonth(), datePicker.getDayOfMonth(),
        timePicker.getCurrentHour(), timePicker.getCurrentMinute());
      long dateTime = c.getTimeInMillis();
      NoticeManager.getInstance().add(
        new Notice(dateTime, header, text));
    } catch (ValidationException ignored) {}
  }
}
