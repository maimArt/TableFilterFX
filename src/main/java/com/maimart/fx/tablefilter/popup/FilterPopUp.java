package com.maimart.fx.tablefilter.popup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.PopupControl;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.VBox;

/**
 * Popup to select filters
 * 
 * @author maim
 *
 * @param <T>
 */
public class FilterPopUp<T> extends PopupControl {

	@FXML
	private VBox rootContent;

	@FXML
	private CheckBox chkBox_selectAll;

	@FXML
	private ListView<T> listView;

	private final List<BooleanProperty> cellCheckboxProps = new ArrayList<>();

	private final ListProperty<T> blacklistItems;

	private boolean selectAllIsRunning = false;

	public FilterPopUp(ListProperty<T> allItems, ListProperty<T> blacklist) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FilterPopUp.fxml"));
		fxmlLoader.setController(this);
		fxmlLoader.setRoot(rootContent);
		try {
			fxmlLoader.load();
		} catch (IOException e) {
			throw new RuntimeException("Could not load fxml", e);
		}

		this.blacklistItems = blacklist;
		listView.itemsProperty().bind(allItems);
		initSelectAllCheckbox();
	}

	public void initialize() {
		setAutoHide(true);
		getScene().setRoot(rootContent);
		defineListView();
	}

	private void initSelectAllCheckbox() {
		chkBox_selectAll.setSelected(true);
		chkBox_selectAll.textProperty()
				.bind(Bindings.when(chkBox_selectAll.selectedProperty()).then("Unselect all").otherwise("Select all"));
		chkBox_selectAll.setOnMouseClicked(event -> {
			// TODO remove selectAllIsRunning hack to avoid setting wrong
			// chkBox_selectAll value
			selectAllIsRunning = true;
			for (BooleanProperty bp : cellCheckboxProps) {
				bp.set(chkBox_selectAll.isSelected());
			}
			selectAllIsRunning = false;
		});
	}

	private void defineListView() {
		CheckBoxListCell<T> checkboxcell = new CheckBoxListCell<>(param -> {
			BooleanProperty property = new SimpleBooleanProperty(true);
			cellCheckboxProps.add(property);
			property.addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
				if (!newValue) {
					blacklistItems.add(param);
				} else {
					blacklistItems.remove(param);
				}
				if (!selectAllIsRunning) {
					chkBox_selectAll.setSelected(blacklistItems.size() == 0);
				}
			});
			return property;
		});
		listView.setCellFactory(CheckBoxListCell.forListView(param -> {
			BooleanProperty property = new SimpleBooleanProperty(true);
			cellCheckboxProps.add(property);
			property.addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
				if (!newValue) {
					blacklistItems.add(param);
				} else {
					blacklistItems.remove(param);
				}
				if (!selectAllIsRunning) {
					chkBox_selectAll.setSelected(blacklistItems.size() == 0);
				}
			});
			return property;
		}));
	}

	public VBox getRootContent() {
		return rootContent;
	}

	public ListView<T> getListView() {
		return listView;
	}

}
