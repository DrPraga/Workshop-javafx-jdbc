package gui;


import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javafx.scene.control.Alert.AlertType;
import model.entities.Department;
import model.entities.Seller;
import model.exception.ValidationException;
import model.services.DepartmentServices;
import model.services.SellerServices;


public class SellerFormController implements Initializable{

	private Seller entitiy;
	
	private SellerServices service;
	
	private DepartmentServices departmentServices;
	
	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();
	
	@FXML
	private TextField txtId; 
	
	@FXML
	private TextField txtName; 
	
	@FXML
	private TextField txtEmail; 
	
	@FXML
	private DatePicker dpBirthDate; 
	
	@FXML
	private TextField txtBaseSalary; 
	
	@FXML
	private ComboBox<Department> comboBoxDepartment;
	
	@FXML
	private Label labelErrorName;

	@FXML
	private Label labelErrorEmail;
	
	@FXML
	private Label labelErrorBirthDate;
	
	@FXML
	private Label labelErrorBaseSalary;
	
	@FXML
	private Button btSave;
	
	@FXML
	private Button btCancel;
	
	private ObservableList<Department> obsList;
	
	public void setSeller(Seller entity) {
		this.entitiy = entity;
	}
	
	public void setService(SellerServices service, DepartmentServices departmentServices) {
		this.service = service;
		this.departmentServices = departmentServices;
	}
	
	public void subscribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}
	
	@FXML
	public void onBtSaveAction(ActionEvent event) {
		if(entitiy == null) {
			throw new IllegalStateException("Entity was null");
		}
		if(service == null) {
			throw new IllegalStateException("Service was null");
		}
		try {
			entitiy = getFormData();
			service.saveOrUpdate(entitiy);
			notifyDataChangeListeners();
			gui.util.Utils.currenceStage(event).close();
		}
		
		catch(ValidationException e) {
			setErrorMessages(e.getErrors());
		}
		
		catch(DbException e) {
			Alerts.showAlert("Error saving object", null, e.getMessage(), AlertType.ERROR);
		}
	}
	private void notifyDataChangeListeners() {
		for(DataChangeListener listener : dataChangeListeners) {
			listener.onDataChange();
		}
		
	}

	private Seller getFormData() {
		Seller obj = new Seller();
		
		ValidationException exception = new ValidationException("Validation error");
		obj.setId(gui.util.Utils.tryParseToInt(txtId.getText()));
		
		if(txtName.getText() == null || txtName.getText().trim().equals("")) {
			exception.addError("name", "Field can't be empty");
		}
		
		obj.setName(txtName.getText());
		
		if(exception.getErrors().size() > 0 ) {
			throw exception;
		}
		return obj;
	}

	@FXML
	public void onBtCancelAction(ActionEvent event) {
		System.out.println("onBtCancelAction");
		gui.util.Utils.currenceStage(event).close();
	}
	
	@Override
	public void initialize(java.net.URL arg0, ResourceBundle arg1) {
		initializeNodes();
	}	
	
	private void initializeNodes() {
		Constraints.setTextFieldInteger(txtId);
		Constraints.setTextFieldMaxLength(txtName, 30);
		Constraints.setTextFieldDouble(txtBaseSalary);
		Constraints.setTextFieldMaxLength(txtEmail, 60);
		Utils.formatDatePicker(dpBirthDate, "dd/MM/yyyy");
		
		initializeComboBoxDepartment();
	}
	
	public void loadAssociatedObjects() {
		if (departmentServices == null) {
			throw new IllegalStateException("DepartmentService was null");
		}
		List<Department> list = departmentServices.findAll();
		obsList = FXCollections.observableArrayList(list);
		comboBoxDepartment.setItems(obsList);
	}
	
	public void updateFormData() {
		if(entitiy == null) {
			throw new IllegalStateException("Entity was null");
		}
		txtId.setText(String.valueOf(entitiy.getId()));
		txtName.setText(entitiy.getName());
		txtEmail.setText(entitiy.getEmail());
		Locale.setDefault(Locale.US);
		txtBaseSalary.setText(String.format("%.2f", entitiy.getBaseSalary()));
		if (entitiy.getBirthDate() != null) {
			dpBirthDate.setValue(LocalDate.ofInstant(entitiy.getBirthDate().toInstant(), ZoneId.systemDefault()) );
		}
		if(entitiy.getDepartment() == null) {
			comboBoxDepartment.getSelectionModel().selectFirst();
		}
		else {
			comboBoxDepartment.setValue(entitiy.getDepartment());
		}
	}
	
	private void setErrorMessages(Map<String, String> errors) {
		 Set<String> fields = errors.keySet();
		 
		 if(fields.contains("name")) {
			 labelErrorName.setText(errors.get("name"));
		 }
		 
	}
	
	private void initializeComboBoxDepartment() { 
		Callback<ListView<Department>, ListCell<Department>> factory = lv -> new ListCell<Department>() { 
			@Override
			protected void updateItem(Department item, boolean empty) { 
				super.updateItem(item, empty); 
				setText(empty ? "" : item.getName()); 
			} 
		}; 
		comboBoxDepartment.setCellFactory(factory); 
		comboBoxDepartment.setButtonCell(factory.call(null)); 
	} 
}
