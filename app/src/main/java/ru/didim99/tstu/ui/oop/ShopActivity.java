package ru.didim99.tstu.ui.oop;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.oop.shop.Bank;
import ru.didim99.tstu.core.oop.shop.Card;
import ru.didim99.tstu.core.oop.shop.Division;
import ru.didim99.tstu.core.oop.shop.Shop;
import ru.didim99.tstu.core.oop.shop.ShopController;
import ru.didim99.tstu.core.oop.shop.products.Food;
import ru.didim99.tstu.core.oop.shop.products.Product;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.ui.DialogEventListener;
import ru.didim99.tstu.ui.SpinnerAdapter;
import ru.didim99.tstu.utils.InputValidator;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 13.03.19.
 */
public class ShopActivity extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_ShopAct";
  private static final String FILENAME = "/shoplist.dat";
  private static final String DIRNAME = "/oop";

  // View-elements
  private Spinner spShop;
  private Spinner spDiv;
  private Button btnBuy;
  private Button btnTakeCredit;
  private Button btnCloseCredit;
  private ProductAdapter shopAdapter;
  private ProductAdapter cardAdapter;
  private ProductAdapter inventoryAdapter;
  private ArrayAdapter<String> divListAdapter;
  private TextView tvTotalPrice;
  private TextView tvClientId;
  private TextView tvMoneyValue;
  private TextView tvCreditLimit;
  private TextView tvCreditValue;
  // Workflow
  private ShopController controller;
  private NumberFormat format;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "ShopActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_oop_shop);

    shopAdapter = new ProductAdapter(this,
      ProductAdapter.Type.SHOP, this::onShopItemClicked);
    cardAdapter = new ProductAdapter(this,
      ProductAdapter.Type.CARD, this::onCardItemClicked);
    inventoryAdapter = new ProductAdapter(this,
      ProductAdapter.Type.INVENTORY, this::onInventoryItemClicked);
    format = NumberFormat.getCurrencyInstance(new Locale("ru"));
    format.setCurrency(Currency.getInstance("RUB"));
    format.setMaximumFractionDigits(0);

    MyLog.d(LOG_TAG, "View components init...");
    spShop = findViewById(R.id.spShop);
    spDiv = findViewById(R.id.spGroup);
    btnBuy = findViewById(R.id.btnBuy);
    btnTakeCredit = findViewById(R.id.btnTakeCredit);
    btnCloseCredit = findViewById(R.id.btnCloseCredit);
    tvTotalPrice = findViewById(R.id.tvCardSum);
    tvClientId = findViewById(R.id.tvAccNumber);
    tvMoneyValue = findViewById(R.id.tvAccValue);
    tvCreditLimit = findViewById(R.id.tvCreditLimit);
    tvCreditValue = findViewById(R.id.tvCreditValue);
    RecyclerView rvShop = findViewById(R.id.rvShop);
    RecyclerView rvCard = findViewById(R.id.rvCard);
    RecyclerView rvInventory = findViewById(R.id.rvInventory);
    rvShop.setLayoutManager(new LinearLayoutManager(this));
    rvCard.setLayoutManager(new LinearLayoutManager(this));
    rvInventory.setLayoutManager(new LinearLayoutManager(this));
    rvShop.setHasFixedSize(true);
    rvCard.setHasFixedSize(true);
    rvInventory.setHasFixedSize(true);
    rvShop.setAdapter(shopAdapter);
    rvCard.setAdapter(cardAdapter);
    rvInventory.setAdapter(inventoryAdapter);
    btnBuy.setOnClickListener(v -> buyCard());
    btnTakeCredit.setOnClickListener(v -> inputFieldDialog(
      R.string.oop_shop_creditSum, R.layout.dia_sf_number, this::takeCredit));
    btnCloseCredit.setOnClickListener(v -> closeCredit());
    findViewById(R.id.btnSalary).setOnClickListener(v -> getSalary());
    MyLog.d(LOG_TAG, "View components init completed");

    controller = (ShopController) getLastCustomNonConfigurationInstance();
    if (controller == null) {
      String path = getExternalCacheDir().getAbsolutePath()
        .concat(DIRNAME).concat(FILENAME);
      controller = new ShopController();
      controller.load(this, path, this::onDataLoaded);
    }

    MyLog.d(LOG_TAG, "ShopActivity started");
  }

  @Override
  public Object onRetainCustomNonConfigurationInstance() {
    return controller;
  }

  private void onDataLoaded() {
    ArrayAdapter<String> shopListAdapter = new ArrayAdapter<>(this,
      android.R.layout.simple_list_item_1, controller.getShopList());
    spShop.setAdapter(shopListAdapter);
    spShop.setOnItemSelectedListener(new SpinnerAdapter(position -> {
      if (controller.getCard().hasItems()) {
        new AlertDialog.Builder(ShopActivity.this)
          .setTitle(R.string.oop_shop_cardNotEmpty)
          .setNegativeButton(R.string.oop_shop_throwCard, (d, v) -> {
            controller.getCard().clear();
            onShopChanged(position);
          })
          .setPositiveButton(R.string.oop_shop_buyCard, (d, v) -> {
            onShopChanged(position);
            buyCard();
          })
          .create().show();
      } else onShopChanged(position);
    }));

    Shop shop = controller.getCurrentShop();
    divListAdapter = new ArrayAdapter<>(this,
      android.R.layout.simple_list_item_1, shop.getDivList());
    spDiv.setAdapter(divListAdapter);
    spDiv.setOnItemSelectedListener(
      new SpinnerAdapter(this::onDivChanged));

    onInventoryStateChanged();
    onCardStateChanged();
    onBankStateChanged();
    onShopChanged(0);
  }

  private void onShopChanged(int id) {
    Shop shop = controller.selectShop(id);
    divListAdapter.clear();
    divListAdapter.addAll(shop.getDivList());
    spDiv.setSelection(shop.getCurrentDivId());
    onDivChanged(shop.getCurrentDivId());
  }

  private void onDivChanged(int id) {
    Division div = controller.getCurrentShop().selectDiv(id);
    shopAdapter.refreshData(div.getProducts());
  }

  private void onCardStateChanged() {
    Card card = controller.getCard();
    btnBuy.setEnabled(card.hasItems());
    tvTotalPrice.setText(format.format(card.getSum()));
    cardAdapter.refreshData(card.getProducts());
  }

  private void onBankStateChanged() {
    Bank bank = controller.getBank();
    tvClientId.setText(String.valueOf(bank.getClientId()));
    tvMoneyValue.setText(format.format(bank.getMoneyValue()));
    tvCreditLimit.setText(format.format(bank.getCreditLimit()));
    tvCreditValue.setText(format.format(bank.getCreditValue()));
    btnTakeCredit.setEnabled(bank.canTakeCredit());
    btnCloseCredit.setEnabled(bank.canCloseCredit());
  }

  private void onInventoryStateChanged() {
    inventoryAdapter.refreshData(controller.getInventory());
  }

  private void buyCard() {
    Card card = controller.getCard();
    if (controller.getBank().getMoneyValue() < card.getSum()) {
      Toast.makeText(this, R.string.errShop_largeSum,
        Toast.LENGTH_LONG).show();
      return;
    }

    String title = getString(R.string.oop_shop_pay,
      format.format(card.getSum()));
    questionDialog(title, (d) -> {
      controller.processBuy();
      onInventoryStateChanged();
      onCardStateChanged();
      onBankStateChanged();
    });
  }

  public void takeCredit(AlertDialog dialog) {
    try {
      Bank bank = controller.getBank();
      InputValidator iv = InputValidator.getInstance();
      EditText etValue = dialog.findViewById(R.id.etInput);
      int sum = iv.checkInteger(etValue, 0, bank.getCreditLimit(),
        R.string.errShop_emptyCredit, R.string.errShop_largeSum, "Sum");
      bank.takeCredit(sum);
      onBankStateChanged();
      dialog.dismiss();
    } catch (InputValidator.ValidationException ignored) {}
  }

  private void closeCredit() {
    Bank bank = controller.getBank();
    questionDialog(getString(R.string.oop_shop_pay,
      format.format(bank.getCreditValue())), (d) -> {
      bank.closeCredit();
      onBankStateChanged();
    });
  }

  private void onShopItemClicked(Product item) {
    MyLog.d(LOG_TAG, "Shop item clicked: " + item);
    String title = getString(R.string.oop_shop_addToCard, item.toString());
    questionDialog(title, d -> {
      MyLog.d(LOG_TAG, "Adding to card: " + item);
      controller.getCard().addProduct(item);
      onCardStateChanged();
    });
  }

  private void onCardItemClicked(Product item) {
    MyLog.d(LOG_TAG, "Card item clicked: " + item.getId());
    String title = getString(R.string.oop_shop_removeFromCard, item.toString());
    questionDialog(title, d -> {
      MyLog.d(LOG_TAG, "Removing from card: " + item);
      controller.getCard().removeProduct(item);
      onCardStateChanged();
    });
  }

  private void onInventoryItemClicked(Product item) {
    MyLog.d(LOG_TAG, "Inventory item clicked: " + item.getId());
    if (item instanceof Food) {
      if (!((Food) item).eat())
        controller.getInventory().remove(item);
      Toast.makeText(this, R.string.oop_shop_timeToEat,
        Toast.LENGTH_LONG).show();
    }
  }

  private void getSalary() {
    MyLog.d(LOG_TAG, "Getting salary");
    int salary = controller.getBank().getSalary();
    onBankStateChanged();
    salaryDialog(salary);
  }

  private void salaryDialog(int salary) {
    MyLog.d(LOG_TAG, "Salary dialog called");
    String str = format.format(salary);
    int titleId = Bank.isGoodSalary(salary) ?
      R.string.oop_shop_goodSalary : R.string.oop_shop_badSalary;
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setPositiveButton(R.string.dialogButtonOk, null);
    adb.setTitle(getString(titleId, str));
    MyLog.d(LOG_TAG, "Salary dialog created");
    adb.create().show();
  }

  private void questionDialog(String title, DialogEventListener listener) {
    MyLog.d(LOG_TAG, "Question dialog called");
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    adb.setPositiveButton(R.string.dialogButtonOk,
      (d, w) -> listener.onEvent((AlertDialog) d));
    adb.setTitle(title);
    MyLog.d(LOG_TAG, "Question dialog created");
    adb.create().show();
  }
}
