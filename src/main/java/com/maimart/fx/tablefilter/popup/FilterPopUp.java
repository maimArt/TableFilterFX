package com.maimart.fx.tablefilter.popup;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
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

	private final Map<T, BooleanProperty> mapItemToSelectedProperty = new HashMap<>();

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
		addSelectionProperties(allItems);
		allItems.addListener((ListChangeListener<T>) c -> {
			while(c.next())
			{
				addSelectionProperties(c.getAddedSubList());
				removeSelectionProperty(c.getRemoved());
			}			
		});
		initSelectAllCheckbox();
	}

	public void initialize() {
		setAutoHide(true);
		getScene().setRoot(rootContent);
		defineListView();
	}
	
	private void addSelectionProperties(List<? extends T> items)
	{
		for(T item : items)
		{
			SimpleBooleanProperty property = new SimpleBooleanProperty(true);
			mapItemToSelectedProperty.put(item, property);
			property.addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
				if (!newValue) {
					blacklistItems.add(item);
				} else {
					blacklistItems.remove(item);
				}
				if (!selectAllIsRunning) {
					chkBox_selectAll.setSelected(blacklistItems.size() == 0);
				}
			});
		}		
	}
	
	private void removeSelectionProperty(List<? extends T> items)
	{
		for(T item : items)
		{
			// TODO remove listener before removing from map
			mapItemToSelectedProperty.remove(item);
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
			for (BooleanProperty bp : mapItemToSelectedProperty.values()) {
				bp.set(chkBox_selectAll.isSelected());
			}
			selectAllIsRunning = false;
		});
	}

	private void defineListView() {
		listView.setCellFactory(CheckBoxListCell.forListView(param -> {
			BooleanProperty property = mapItemToSelectedProperty.get(param);			
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
