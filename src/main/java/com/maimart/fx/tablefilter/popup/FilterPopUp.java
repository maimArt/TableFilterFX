package com.maimart.fx.tablefilter.popup;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.PopupControl;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

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

	private final Map<T, SimpleBooleanProperty> mapCellValueToSelectedProperty = new HashMap<>();

	private final ListProperty<T> blacklistedCellValues;

	private boolean selectAllIsRunning = false;

	private DoubleProperty maxCellWidth = new SimpleDoubleProperty(0);

	public FilterPopUp(ListProperty<T> allCellValues, ListProperty<T> blacklistedCellValues) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FilterPopUp.fxml"));
		fxmlLoader.setController(this);
		fxmlLoader.setRoot(rootContent);
		try {
			fxmlLoader.load();
		} catch (IOException e) {
			throw new RuntimeException("Could not load fxml", e);
		}

		this.blacklistedCellValues = blacklistedCellValues;
		listView.itemsProperty().bind(allCellValues);
		addSelectionProperties(allCellValues);
		allCellValues.addListener((ListChangeListener<T>) c -> {
			mapCellValueToSelectedProperty.clear();
			addSelectionProperties(c.getList());
			chkBox_selectAll.selectedProperty().set(true);
		});
		initSelectAllCheckbox();
	}

	public void initialize() {
		setAutoHide(true);
		getScene().setRoot(rootContent);
		defineListView();
	}

	private void addSelectionProperties(List<? extends T> items) {
		maxCellWidth.set(0);
		listView.setPrefHeight(7);
		for (T item : items) {
			SimpleBooleanProperty property = new SimpleBooleanProperty(true);
			mapCellValueToSelectedProperty.put(item, property);
			property.addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
				if (!newValue) {
					blacklistedCellValues.add(item);
				} else {
					blacklistedCellValues.remove(item);
				}
				if (!selectAllIsRunning) {
					chkBox_selectAll.setSelected(blacklistedCellValues.size() == 0);
				}
			});
		}
		listView.setPrefHeight(listView.getPrefHeight() + (items.size() * listView.getFixedCellSize()));
	}

	private void removeSelectionProperty(List<? extends T> items) {
		for (T item : items) {
			// TODO remove listener before removing from map
			mapCellValueToSelectedProperty.remove(item);
		}
	}

	private void initSelectAllCheckbox() {
		chkBox_selectAll.setSelected(true);
		chkBox_selectAll.textProperty()
				.bind(Bindings.when(chkBox_selectAll.selectedProperty()).then("Unselect all").otherwise("Select all"));
		chkBox_selectAll.setOnMouseClicked(event -> {
			// TODO remove selectAllIsRunning hack to avoid setting wrong
			// chkBox_selectAll value
			selectAllIsRunning = true;
			for (BooleanProperty bp : mapCellValueToSelectedProperty.values()) {
				bp.set(chkBox_selectAll.isSelected());
			}
			selectAllIsRunning = false;
		});
	}

	private void defineListView() {
		listView.prefWidthProperty().bind(maxCellWidth);
		listView.setCellFactory(param -> {
			Callback<T, ObservableValue<Boolean>> callback = param1 -> {
				SimpleBooleanProperty property = mapCellValueToSelectedProperty.get(param1);
				return property;
			};
			CheckBoxListCell<T> checkboxCell = new CheckBoxListCell<>(callback);
			checkboxCell.widthProperty().addListener((ChangeListener<Number>) (observable, oldValue, newValue) -> {
				double width = newValue.doubleValue() + listView.getInsets().getLeft() + listView.getInsets().getRight()
						+ 25;
				if (maxCellWidth.get() < width) {
					maxCellWidth.set(width);
				}
			});

			return checkboxCell;
		});
	}

	public VBox getRootContent() {
		return rootContent;
	}

	public ListView<T> getListView() {
		return listView;
	}

}
