package eu.highq.qonverter;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import eu.highq.qonverter.database.Category;
import eu.highq.qonverter.database.EnergyCarrier;
import eu.highq.qonverter.database.Unit;
import eu.highq.qonverter.database.UnitAbbreviation;
import eu.highq.qonverter.database.Variant;
import eu.highq.qonverter.models.CompareItem;

public class ActCompare extends AppCompatActivity {

    private List<CompareItem> items = new ArrayList<>(2);

    public TextView upperItem, lowerItem;
    public TextView upperItemCategory, lowerItemCategory;

    public Spinner spinnerVariante;

    public static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Main Activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_compare);

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Floating Action Button (Invisible)
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        upperItem = (TextView) findViewById(R.id.txtItemUpper);
        lowerItem = (TextView) findViewById(R.id.txtItemLower);
        upperItemCategory = (TextView) findViewById(R.id.txtItemUpperCategory);
        lowerItemCategory = (TextView) findViewById(R.id.txtItemLowerCategory);
        spinnerVariante = (Spinner) findViewById(R.id.spinnerVariante);

        //OnClickListener for Items
        upperItem.setOnClickListener(itemUpperOnClick);
        lowerItem.setOnClickListener(itemLowerOnClick);

        EditText value = (EditText) findViewById(R.id.edtValueUpper);
        value.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFokus) {
                if (!hasFokus) {
                    CompareItem item = items.get(0);
                    double value = Double.parseDouble(((EditText) view).getText().toString());

                    item.factor = value;
                    CompareItem other = getOther(0);
                    if (other != null) {
                        updateItem(other, 1);
                    }
                } else {
                    ((EditText) view).setText("");
                }
            }
        });
        value.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                return false;
            }
        });

        value = (EditText) findViewById(R.id.edtValueLower);
        value.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFokus) {
                if (!hasFokus) {
                    CompareItem item = items.get(1);
                    double value = Double.parseDouble(((EditText) view).getText().toString());
                    item.factor = value;
                    CompareItem other = getOther(1);
                    if (other != null) {
                        updateItem(other, 0);
                    }
                } else {
                    ((EditText) view).setText("");
                }
            }
        });

        this.items.add(null);
        this.items.add(null);

        if (new Select().from(EnergyCarrier.class).count() == 0) {
            Category.prePopulate();
            Unit.prePopulate();
            UnitAbbreviation.prePopulate();
            EnergyCarrier.prePopulate();
            Variant.prePopulate();
        }

        CompareItem firstItem = generateRandomItem();
        CompareItem secondItem;

        do {
            secondItem = generateRandomItem();
        } while (firstItem.carrier.name == secondItem.carrier.name);

        setItem(firstItem, 0);
        setItem(secondItem, 1);
    }

    private CompareItem getOther(int index) {
        return this.items.get(index == 0 ? 1 : 0);
    }

    private CompareItem generateRandomItem() {
        EnergyCarrier carrier = new Select().from(EnergyCarrier.class).orderBy("RANDOM()").executeSingle();
        return new CompareItem(carrier);
    }

    View.OnClickListener itemUpperOnClick = new View.OnClickListener() {
        public void onClick(View view) {
            Intent ItemSelectionIntent = new Intent(view.getContext(), ActItemSelect.class);
            ItemSelectionIntent.putExtra("item", "1");
            //ItemSelectionIntent.setAction(ItemSelectionIntent.ACTION_ANSWER);
            ActCompare.this.startActivityForResult(ItemSelectionIntent, REQUEST_CODE);
        }
    };

    View.OnClickListener itemLowerOnClick = new View.OnClickListener() {
        public void onClick(View view) {
            Intent ItemSelectionIntent = new Intent(view.getContext(), ActItemSelect.class);
            ItemSelectionIntent.putExtra("item", "2");
            ActCompare.this.startActivityForResult(ItemSelectionIntent, REQUEST_CODE);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            Toast.makeText(this, data.getExtras().getString("item"), Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this, "called", Toast.LENGTH_SHORT).show();
        }


    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle result = getIntent().getExtras();
        String upperOrLower = result.getString("upperOrLower");
        String item = result.getString("item");
        Toast.makeText(this, upperOrLower + ", " + item, Toast.LENGTH_LONG).show();
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_act_compare, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_add) {
            //setContentView(R.layout.popup_addcategory);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void setItem(CompareItem item, int index) {
        if ((index < 0) || (index >= this.items.size())) {
            throw new IllegalArgumentException("Index out of bounds");
        }

        this.items.set(index, item);

        updateItem(item, index);
    }

    private void updateItem(CompareItem item, int index) {
        CompareItem compareWith = getOther(index);

        if (compareWith != null) {
            item.adjustFactor(compareWith.calculateEnergy());
        }

        displayItem(item, index);
    }

    private void displayItem(CompareItem item, int index) {
        switch (index) {
            case 0:
                // hier Oberfläche 1 updaten
                upperItem.setText(item.carrier.name);

                //Toast.makeText(this, item.carrier.variants().size(), Toast.LENGTH_LONG).show();

                List<Variant> variants = item.carrier.variants();
                
                List<String> variantNames = new ArrayList<String>();
                for (Variant v : variants) {
                    variantNames.add(v.name);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, variantNames);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerVariante.setAdapter(adapter);

                upperItemCategory.setText(item.carrier.category.name);

                TextView label = (TextView) findViewById(R.id.txtLabelUpper);
                label.setText(item.carrier.unit.name);

                UnitAbbreviation abbr = item.carrier.unit.abbreviations().get(0);

                TextView unit = (TextView) findViewById(R.id.txtUnitUpper);
                unit.setText(abbr.abbreviation);

                EditText value = (EditText) findViewById(R.id.edtValueUpper);
                value.setText(String.format("%.2f", (item.factor * abbr.factor)));

                break;
            case 1:
                // hier Oberfläche 2 updaten
                lowerItem.setText(item.carrier.name);
                lowerItemCategory.setText(item.carrier.category.name);

                label = (TextView) findViewById(R.id.txtLabelLower);
                label.setText(item.carrier.unit.name);

                abbr = item.carrier.unit.abbreviations().get(0);

                unit = (TextView) findViewById(R.id.txtUnitLower);
                unit.setText(abbr.abbreviation);

                value = (EditText) findViewById(R.id.edtValueLower);
                value.setText(String.format("%.2f", (item.factor * abbr.factor)));

                break;
            default:
                throw new IllegalArgumentException("Index out of bounds");
        }
    }
}
