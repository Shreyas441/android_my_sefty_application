package com.thesavior.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.thesavior.utilities.Information;
import com.thesavior.utilities.SharedPreference;

public class SetupActivity extends FragmentActivity implements OnClickListener {
	/** Called when the activity is first created. */
	private static final String TAG = "SETUP ACTIVITY";
	private Button back, about, save;
	private Button step1_msg, step2_gps, step3_contact;
	private Button add_contact1, add_contact2, add_contact3;
	private TextView heading;
	private RelativeLayout rLayout_msg;
	private RelativeLayout rLayout_gps;
	private RelativeLayout rLayout_contact;
	private Animation Left_in_animation;
	private Animation Right_in_animation;
	private Animation Left_out_animation;
	private Animation Right_out_animation;
	private AutoCompleteTextView phone_no1;
	private AutoCompleteTextView phone_no3;
	private EditText send_message1;
	private EditText phone_no2ForMail;
	private int countStep = 1;
	private static final int DIALOG_MULTIPLE_CHOICE_CURSOR = 1;
	private static final int DIALOG_SINGLE_CHOICE_CURSOR = 2;
	private CharSequence name[];
	private CharSequence number[];
	private CharSequence nameWithNumber[];
	private boolean checked[];
	private CharSequence Selected_Numbers;
	private ArrayList<String> Selected = new ArrayList<String>();
	private ArrayList<Map<String, String>> mPeopleList = new ArrayList<Map<String, String>>();
	private SimpleAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_setup);
		rLayout_msg = (RelativeLayout) findViewById(R.id.linearLayout_step1);// msg
		rLayout_gps = (RelativeLayout) findViewById(R.id.linearLayout_step2);// gps
		rLayout_contact = (RelativeLayout) findViewById(R.id.linearLayout_step3);// contact
		rLayout_msg.setVisibility(View.VISIBLE);
		rLayout_gps.setVisibility(View.GONE);
		rLayout_contact.setVisibility(View.GONE);
		Left_in_animation = AnimationUtils.loadAnimation(this,
				R.anim.slide_in_left);
		Right_in_animation = AnimationUtils.loadAnimation(this,
				R.anim.slide_in_right);
		Left_out_animation = AnimationUtils.loadAnimation(this,
				R.anim.slide_out_left);
		Right_out_animation = AnimationUtils.loadAnimation(this,
				R.anim.slide_out_right);

		step1_msg = (Button) findViewById(R.id.btn_step1_msg);
		step1_msg.setOnClickListener(this);
		step1_msg.setBackgroundResource(R.drawable.step1_chk);

		step2_gps = (Button) findViewById(R.id.btn_step2_gps);
		step2_gps.setOnClickListener(this);

		step3_contact = (Button) findViewById(R.id.btn_step3_contact);
		step3_contact.setOnClickListener(this);

		heading = (TextView) findViewById(R.id.textView_heading);
		heading.setText(getResources()
				.getString(R.string.text_header_SetupPage));

		save = (Button) findViewById(R.id.btn_save);
		save.setText(getResources().getString(R.string.button_next));

		PopulatePeopleList();
		phone_no1 = (AutoCompleteTextView) findViewById(R.id.editText_phone_no1);
		send_message1 = (EditText) findViewById(R.id.editText_message1);
		mAdapter = new SimpleAdapter(this, mPeopleList,
				R.layout.custom_contact_view, new String[] { "Name", "Phone" },
				new int[] { R.id.ccontName, R.id.ccontNo });
		phone_no1.setAdapter(mAdapter);
		Log.d("contact", "adapterval : " + mAdapter);
		Log.d("contact", "adapterval length : " + mAdapter.getCount());
		phone_no1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View arg1,
					int position, long arg3) {
				@SuppressWarnings("unchecked")
				Map<String, String> map = (Map<String, String>) parent
						.getItemAtPosition(position);
				String name = map.get("Name");
				String number = map.get("Phone");
				phone_no1.setText(number.trim());
				phone_no1.setSelection(phone_no1.getText().length());
//				InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);  
//				imm.hideSoftInputFromWindow(getWindowToken(), 0); 

			}
		});
		phone_no1.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.length() > 0)
					save.setText(getResources().getString(
							R.string.button_save_next));
				else
					save.setText(getResources().getString(R.string.button_next));
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			public void afterTextChanged(Editable s) {

			}
		});
		add_contact1 = (Button) findViewById(R.id.btn_addContact1);
		add_contact1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				phone_no1.requestFocus();
				showDatePicker(DIALOG_MULTIPLE_CHOICE_CURSOR);
			}
		});
		phone_no2ForMail = (EditText) findViewById(R.id.editText_EmailID_no2);
		phone_no2ForMail.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.length() > 0)
					save.setText(getResources().getString(
							R.string.button_save_next));
				else
					save.setText(getResources().getString(R.string.button_next));
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			public void afterTextChanged(Editable s) {

			}
		});
		phone_no2ForMail
				.setOnEditorActionListener(new OnEditorActionListener() {
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						// TODO Auto-generated method stub
						if (actionId == EditorInfo.IME_ACTION_DONE) {
							if (!Information.isEmailValid(phone_no2ForMail
									.getText().toString())) {
								phone_no2ForMail
										.setError("Oops! Please enter valid email id.");
								phone_no2ForMail.selectAll();
								phone_no2ForMail.setSelectAllOnFocus(true);
								save.setText(getResources().getString(
										R.string.button_next));
								return true;
							} else if (phone_no2ForMail.getText().length() == 0) {
								phone_no2ForMail
										.setError("This field cannot be blank!");
								save.setText(getResources().getString(
										R.string.button_next));
								return true;
							}
							save.setText(getResources().getString(
									R.string.button_save_next));
						}
						return false;
					}
				});

		add_contact2 = (Button) findViewById(R.id.btn_addContact2);
		add_contact2.setVisibility(View.GONE);

		phone_no3 = (AutoCompleteTextView) findViewById(R.id.editText_phone_no3);
		phone_no3.setAdapter(mAdapter);
		phone_no3.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View arg1,
					int position, long arg3) {
				@SuppressWarnings("unchecked")
				Map<String, String> map = (Map<String, String>) parent
						.getItemAtPosition(position);
				String name = map.get("Name");
				String number = map.get("Phone");
				phone_no3.setText(number.trim());
				phone_no3.setSelection(phone_no3.getText().length());

			}
		});
		phone_no3.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.length() > 0)
					save.setText(getResources().getString(
							R.string.button_save_next));
				else
					save.setText(getResources().getString(R.string.button_next));
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			public void afterTextChanged(Editable s) {

			}
		});
		add_contact3 = (Button) findViewById(R.id.btn_addContact3);
		add_contact3.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				phone_no3.requestFocus();
				showDatePicker(DIALOG_SINGLE_CHOICE_CURSOR);
			}
		});
		setLayout();
		back = (Button) findViewById(R.id.btn_back);
		back.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (countStep == 1) {
					back_setStep1();
					setButtonText();
				} else if (countStep == 2) {
					back_setStep2();
					setButtonText();
				} else if (countStep == 3) {
					back_setStep3();
					setButtonText();
				}
			}
		});
		about = (Button) findViewById(R.id.btn_about_info);
		about.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				CustomDialogClass customDialog = new CustomDialogClass(
						SetupActivity.this, false, getResources().getString(
								R.string.Desc_setup_page), getResources()
								.getString(R.string.app_name_inSmall));
				customDialog.show();
			}
		});
		save.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (countStep == 1) {
					setStep1();
					setButtonText();
				} else if (countStep == 2) {
					setStep2();
					setButtonText();
				} else if (countStep == 3) {
					setStep3();
					setButtonText();
				}
			}
		});
		getDataFromPrefrence();

	}

	/**
	 * Get All the Contact Names
	 * 
	 */

	@SuppressWarnings("deprecation")
	public void PopulatePeopleList() {
		mPeopleList.clear();
		Cursor people = getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

		while (people.moveToNext()) {
			String contactName = people.getString(people
					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

			String contactId = people.getString(people
					.getColumnIndex(ContactsContract.Contacts._ID));
			String hasPhone = people
					.getString(people
							.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

			if ((Integer.parseInt(hasPhone) > 0)) {

				// You know have the number so now query it like this
				Cursor phones = getContentResolver().query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null,
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID
								+ " = " + contactId, null, null);
				while (phones.moveToNext()) {

					// store numbers and display a dialog letting the user
					// select which.
					String phoneNumber = phones
							.getString(phones
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

					String numberType = phones
							.getString(phones
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

					Map<String, String> NamePhoneType = new HashMap<String, String>();

					NamePhoneType.put("Name", contactName);
					NamePhoneType.put("Phone", phoneNumber);

					if (numberType.equals("0"))
						NamePhoneType.put("Type", "Work");
					else if (numberType.equals("1"))
						NamePhoneType.put("Type", "Home");
					else if (numberType.equals("2"))
						NamePhoneType.put("Type", "Mobile");
					else
						NamePhoneType.put("Type", "Other");

					// Then add this map to the list.
					mPeopleList.add(NamePhoneType);
				}
				phones.close();
			}
		}
		people.close();
		startManagingCursor(people);
		Log.v("contact", "Array list : " + mPeopleList);

	}

	private void getDataFromPrefrence() {
		if (!SharedPreference.getdata(SetupActivity.this, "step1_phone")
				.equalsIgnoreCase("")
				|| !SharedPreference.getdata(SetupActivity.this, "step1_msg")
						.equalsIgnoreCase("")) {
			phone_no1.setText(SharedPreference.getdata(SetupActivity.this,
					"step1_phone"));
			phone_no1.setSelection(phone_no1.getText().length());
			send_message1.setText(SharedPreference.getdata(SetupActivity.this,
					"step1_msg"));
			send_message1.setSelection(send_message1.getText().length());
		}
		if (!SharedPreference.getdata(SetupActivity.this, "step2_phone")
				.equalsIgnoreCase("")
				|| !SharedPreference.getdata(SetupActivity.this, "step2_msg")
						.equalsIgnoreCase("")) {
			phone_no2ForMail.setText(SharedPreference.getdata(
					SetupActivity.this, "step2_phone"));
			phone_no2ForMail.setSelection(phone_no2ForMail.getText().length());
		}
		if (!SharedPreference.getdata(SetupActivity.this, "step3_phone")
				.equalsIgnoreCase("")
				|| !SharedPreference.getdata(SetupActivity.this, "step3_msg")
						.equalsIgnoreCase("")) {
			phone_no3.setText(SharedPreference.getdata(SetupActivity.this,
					"step3_phone"));
			phone_no3.setSelection(phone_no3.getText().length());

		}
	}

	private void callSteps() {
		if (countStep == 1) {
//			if (!phone_no1.getText().toString().equalsIgnoreCase("")) {
//				// && !send_message1.getText().toString().equalsIgnoreCase("")
//				SharedPreference.putdata(SetupActivity.this, "step1_phone",
//						phone_no1.getText().toString());
//				SharedPreference.putdata(SetupActivity.this, "step1_msg",
//						send_message1.getText().toString());
//			}el
			SharedPreference.putdata(SetupActivity.this, "step1_phone",
					phone_no1.getText().toString());
			SharedPreference.putdata(SetupActivity.this, "step1_msg",
					send_message1.getText().toString());
		}
		if (countStep == 2) {
//			if (!phone_no2ForMail.getText().toString().equalsIgnoreCase("")) {
//				SharedPreference.putdata(SetupActivity.this, "step2_phone",
//						phone_no2ForMail.getText().toString());
//			}
			SharedPreference.putdata(SetupActivity.this, "step2_phone",
					phone_no2ForMail.getText().toString());
		}
		if (countStep == 3) {
//			if (!phone_no3.getText().toString().equalsIgnoreCase("")) {
//				SharedPreference.putdata(SetupActivity.this, "step3_phone",
//						phone_no3.getText().toString());
//			}
			SharedPreference.putdata(SetupActivity.this, "step3_phone",
					phone_no3.getText().toString());
		}
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_step1_msg:
			if (countStep == 1) {

			} else {
				if (countStep == 2) {
					rLayout_msg.startAnimation(Right_in_animation);
					rLayout_gps.startAnimation(Right_out_animation);
				}
				if (countStep == 3) {
					rLayout_msg.startAnimation(Right_in_animation);
					rLayout_contact.startAnimation(Right_out_animation);
				}
				step1_msg.setBackgroundResource(R.drawable.step1_chk);
				step2_gps.setBackgroundResource(R.drawable.step1);
				step3_contact.setBackgroundResource(R.drawable.step1);
				step1_msg.setTextColor(getResources().getColor(
						R.color.MEDIUM_YELLOW));
				step3_contact.setTextColor(Color.WHITE);
				step2_gps.setTextColor(Color.WHITE);
				rLayout_msg.setVisibility(View.VISIBLE);
				rLayout_gps.setVisibility(View.GONE);
				rLayout_contact.setVisibility(View.GONE);
				callSteps();
				countStep = 1;
				setButtonText();
			}
			break;
		case R.id.btn_step2_gps:
			if (countStep == 2) {

			} else {
				if (countStep == 1) { // forward animation
					rLayout_gps.startAnimation(Left_in_animation);
					rLayout_msg.startAnimation(Left_out_animation);
				} else {// back animation
					rLayout_gps.startAnimation(Right_in_animation);
					rLayout_contact.startAnimation(Right_out_animation);
				}
				step1_msg.setBackgroundResource(R.drawable.step1);
				step2_gps.setBackgroundResource(R.drawable.step1_chk);
				step3_contact.setBackgroundResource(R.drawable.step1);
				step1_msg.setTextColor(Color.WHITE);
				step2_gps.setTextColor(getResources().getColor(
						R.color.MEDIUM_YELLOW));
				step3_contact.setTextColor(Color.WHITE);
				rLayout_gps.setVisibility(View.VISIBLE);
				rLayout_msg.setVisibility(View.GONE);
				rLayout_contact.setVisibility(View.GONE);
				callSteps();
				countStep = 2;
				setButtonText();
			}
			break;
		case R.id.btn_step3_contact:
			if (countStep == 3) {

			} else {
				if (countStep == 2 || countStep == 1) {
					rLayout_contact.startAnimation(Left_in_animation);
					rLayout_gps.startAnimation(Left_out_animation);
				}
				rLayout_contact.startAnimation(Left_in_animation);
				rLayout_gps.startAnimation(Left_out_animation);
				step1_msg.setBackgroundResource(R.drawable.step1);
				step2_gps.setBackgroundResource(R.drawable.step1);
				step3_contact.setBackgroundResource(R.drawable.step1_chk);
				step1_msg.setTextColor(Color.WHITE);
				step2_gps.setTextColor(Color.WHITE);
				step3_contact.setTextColor(getResources().getColor(
						R.color.MEDIUM_YELLOW));
				rLayout_contact.setVisibility(View.VISIBLE);
				rLayout_msg.setVisibility(View.GONE);
				rLayout_gps.setVisibility(View.GONE);
				callSteps();
				countStep = 3;
				setButtonText();
			}
			break;
		}
	}

	private void setLayout() {
		if (countStep == 1) {
			step1_msg.setBackgroundResource(R.drawable.step1_chk);
			step2_gps.setBackgroundResource(R.drawable.step1);
			step3_contact.setBackgroundResource(R.drawable.step1);
			step1_msg.setTextColor(getResources().getColor(
					R.color.MEDIUM_YELLOW));
			step2_gps.setTextColor(Color.WHITE);
			step3_contact.setTextColor(Color.WHITE);
			rLayout_msg.setVisibility(View.VISIBLE);
			rLayout_gps.setVisibility(View.GONE);
			rLayout_contact.setVisibility(View.GONE);
			callSteps();
			countStep = 1;
		} else if (countStep == 2) {
			step1_msg.setBackgroundResource(R.drawable.step1);
			step2_gps.setBackgroundResource(R.drawable.step1_chk);
			step3_contact.setBackgroundResource(R.drawable.step1);
			step1_msg.setTextColor(Color.WHITE);
			step2_gps.setTextColor(getResources().getColor(
					R.color.MEDIUM_YELLOW));
			step3_contact.setTextColor(Color.WHITE);
			rLayout_gps.setVisibility(View.VISIBLE);
			rLayout_msg.setVisibility(View.GONE);
			rLayout_contact.setVisibility(View.GONE);
			callSteps();
			countStep = 2;
		} else if (countStep == 3) {
			step1_msg.setBackgroundResource(R.drawable.step1);
			step2_gps.setBackgroundResource(R.drawable.step1);
			step3_contact.setBackgroundResource(R.drawable.step1_chk);
			step1_msg.setTextColor(Color.WHITE);
			step2_gps.setTextColor(Color.WHITE);
			step3_contact.setTextColor(getResources().getColor(
					R.color.MEDIUM_YELLOW));
			rLayout_contact.setVisibility(View.VISIBLE);
			rLayout_msg.setVisibility(View.GONE);
			rLayout_gps.setVisibility(View.GONE);
			callSteps();
			countStep = 3;
		}
		setButtonText();

	}

	// ************ STEP 1
	private void setStep1() {

		callSteps();
		rLayout_gps.startAnimation(Left_in_animation);
		rLayout_msg.startAnimation(Left_out_animation);
		step1_msg.setBackgroundResource(R.drawable.step1);
		step2_gps.setBackgroundResource(R.drawable.step1_chk);
		step3_contact.setBackgroundResource(R.drawable.step1);
		step1_msg.setTextColor(Color.WHITE);
		step2_gps.setTextColor(getResources().getColor(R.color.MEDIUM_YELLOW));
		step3_contact.setTextColor(Color.WHITE);
		rLayout_gps.setVisibility(View.VISIBLE);
		rLayout_msg.setVisibility(View.GONE);
		rLayout_contact.setVisibility(View.GONE);
		countStep = 2;
	}

	private void back_setStep1() {
		callBackAndHomePage();
	}

	// ************ STEP 2
	private void setStep2() {
		callSteps();
		rLayout_contact.startAnimation(Left_in_animation);
		rLayout_gps.startAnimation(Left_out_animation);
		step1_msg.setBackgroundResource(R.drawable.step1);
		step2_gps.setBackgroundResource(R.drawable.step1);
		step3_contact.setBackgroundResource(R.drawable.step1_chk);
		step1_msg.setTextColor(Color.WHITE);
		step2_gps.setTextColor(Color.WHITE);
		step3_contact.setTextColor(getResources().getColor(
				R.color.MEDIUM_YELLOW));
		rLayout_contact.setVisibility(View.VISIBLE);
		rLayout_msg.setVisibility(View.GONE);
		rLayout_gps.setVisibility(View.GONE);
		countStep = 3;
	}

	private void back_setStep2() {
		rLayout_msg.startAnimation(Right_in_animation);
		rLayout_gps.startAnimation(Right_out_animation);
		step1_msg.setBackgroundResource(R.drawable.step1_chk);
		step2_gps.setBackgroundResource(R.drawable.step1);
		step3_contact.setBackgroundResource(R.drawable.step1);
		step1_msg.setTextColor(getResources().getColor(R.color.MEDIUM_YELLOW));
		step2_gps.setTextColor(Color.WHITE);
		step3_contact.setTextColor(Color.WHITE);
		rLayout_msg.setVisibility(View.VISIBLE);
		rLayout_gps.setVisibility(View.GONE);
		rLayout_contact.setVisibility(View.GONE);
		countStep = 1;
	}

	// ************ STEP 3
	private void setStep3() {
		callSteps();
		goToSettingsPage();
		// callBack();
	}

	private void back_setStep3() {
		rLayout_gps.startAnimation(Right_in_animation);
		rLayout_contact.startAnimation(Right_out_animation);
		step1_msg.setBackgroundResource(R.drawable.step1);
		step2_gps.setBackgroundResource(R.drawable.step1_chk);
		step3_contact.setBackgroundResource(R.drawable.step1);
		step1_msg.setTextColor(Color.WHITE);
		step2_gps.setTextColor(getResources().getColor(R.color.MEDIUM_YELLOW));
		step3_contact.setTextColor(Color.WHITE);
		rLayout_gps.setVisibility(View.VISIBLE);
		rLayout_msg.setVisibility(View.GONE);
		rLayout_contact.setVisibility(View.GONE);
		countStep = 2;
	}

	private void setButtonText() {
		if (countStep == 1)
			if (phone_no1.getText().toString().equalsIgnoreCase("")) {
				save.setText(getResources().getString(R.string.button_next));
			} else
				save.setText(getResources()
						.getString(R.string.button_save_next));
		if (countStep == 2)
			if (phone_no2ForMail.getText().length() == 0) {
				save.setText(getResources().getString(R.string.button_next));
			} else
				save.setText(getResources()
						.getString(R.string.button_save_next));
		if (countStep == 3)
			if (phone_no3.getText().length() == 0) {
				save.setText(getResources().getString(R.string.button_next));
			} else
				save.setText(getResources()
						.getString(R.string.button_save_next));
	}

	private void showDatePicker(int callId) {
		ContactDialogFragment contactDialogFragment = new ContactDialogFragment();
		Bundle args = new Bundle();
		args.putInt("dialog_id", callId);
		contactDialogFragment.setArguments(args);
		contactDialogFragment.show(getSupportFragmentManager(),
				"Contact Dialog");
	}

	@SuppressLint("ValidFragment")
	public class ContactDialogFragment extends DialogFragment {
		public Cursor cursor;

		public ContactDialogFragment() {
			String[] projection = new String[] { Phone._ID, Phone.DISPLAY_NAME,
					Phone.NUMBER };

			String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
					+ " COLLATE LOCALIZED ASC";
			cursor = getContentResolver().query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
					projection, null, null, sortOrder);
			if (cursor != null) {
				name = new String[cursor.getCount()];
				number = new String[cursor.getCount()];
				nameWithNumber = new String[cursor.getCount()];
				checked = new boolean[cursor.getCount()];
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					name[i] = cursor.getString(1);
					number[i] = cursor.getString(2);
					if (name[i].equals(number[i]))
						nameWithNumber[i] = name[i].toString();
					else
						nameWithNumber[i] = name[i].toString() + "\n"
								+ number[i].toString();
					cursor.moveToNext();
					if (cursor.isAfterLast())
						break;
				}
			}
		}

		private int callId;

		@Override
		public void setArguments(Bundle args) {
			super.setArguments(args);
			callId = args.getInt("dialog_id");
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			switch (callId) {
			case DIALOG_MULTIPLE_CHOICE_CURSOR:

				return new AlertDialog.Builder(SetupActivity.this)
						.setIcon(android.R.drawable.sym_contact_card)
						.setTitle("Select Contacts")
						.setMultiChoiceItems(
								nameWithNumber,
								checked,
								new DialogInterface.OnMultiChoiceClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton, boolean isChecked) {
										Selected.clear();
										if ((checked[Integer
												.valueOf(whichButton)] == true) == (((AlertDialog) dialog)
												.getListView().isItemChecked(
														whichButton) == true)) {
											((AlertDialog) dialog)
													.getListView()
													.setItemChecked(
															whichButton, false);
										} else {
											((AlertDialog) dialog)
													.getListView()
													.setItemChecked(
															whichButton, true);
										}
									}
								})
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {

										for (int i = 0; i < name.length; i++) {
											if (((AlertDialog) dialog)
													.getListView()
													.isItemChecked(i)) {
												Selected_Numbers = number[i];
												Selected.add(Selected_Numbers
														.toString().trim());
											}
										}
										if (phone_no1.getText().length() > 0
												&& !Selected.isEmpty()) {
											phone_no1.append(","
													+ Selected
															.toString()
															.replaceAll(
																	"\\[|\\]",
																	"").trim());
											phone_no1.setSelection(phone_no1
													.getText().length());
										} else {
											phone_no1.append(Selected
													.toString()
													.replaceAll("\\[|\\]", "")
													.trim());
											phone_no1.setSelection(phone_no1
													.getText().length());
										}

										Selected_Numbers = "";
										Selected.clear();
										dialog.dismiss();
									}
								}).create();
			case DIALOG_SINGLE_CHOICE_CURSOR:

				return new AlertDialog.Builder(SetupActivity.this)
						.setTitle("Select Contact")
						.setIcon(android.R.drawable.sym_contact_card)
						.setSingleChoiceItems(cursor, 0,
								ContactsContract.Contacts.DISPLAY_NAME,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {

									}
								})
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										for (int i = 0; i < name.length; i++) {
											if (((AlertDialog) dialog)
													.getListView()
													.isItemChecked(i)) {
												Selected_Numbers = number[i];
												phone_no3
														.setText(Selected_Numbers
																.toString());
												phone_no3
														.setSelection(phone_no3
																.getText()
																.length());
											}
										}
										dialog.dismiss();
									}
								}).create();
			}
			return null;

		}
	}

	private void callBackAndHomePage() {
		startActivity(new Intent(SetupActivity.this, HomeActivity.class));
		SetupActivity.this.finish();
	}

	private void goToSettingsPage() {
		startActivity(new Intent(SetupActivity.this, SettingActivity.class));
		SetupActivity.this.finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (countStep == 1) {
				back_setStep1();
				setButtonText();
			} else if (countStep == 2) {
				back_setStep2();
				setButtonText();
			} else if (countStep == 3) {
				back_setStep3();
				setButtonText();
			}
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG,
				"STEP 1 DATA : \n contacts : "
						+ SharedPreference.getdata(SetupActivity.this,
								"step1_phone")
						+ "\n messages : "
						+ SharedPreference.getdata(SetupActivity.this,
								"step1_msg"));
		Log.d(TAG,
				"STEP 2 DATA : \n contacts : "
						+ SharedPreference.getdata(SetupActivity.this,
								"step2_phone"));
		Log.d(TAG,
				"STEP 3 DATA : \n contacts : "
						+ SharedPreference.getdata(SetupActivity.this,
								"step3_phone"));

		SetupActivity.this.finish();
	}
}