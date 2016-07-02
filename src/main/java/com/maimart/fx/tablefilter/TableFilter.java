package com.maimart.fx.tablefilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Filtered table
 * 
 * Do not use itemsProperty(), getItems() or setItems()!!!
 * 
 * @author maim
 *
 * @param <S>
 */
public class TableFilter<S> {

	private final TableView<S> tableView;
	private final Map<Integer, List<ColumnFilter<S, ?>>> mapRowIndexToBlacklistingColumn = new HashMap<>();
	private final ListProperty<S> unfilteredItems = new SimpleListProperty<S>(FXCollections.observableArrayList());

	/**
	 * Constructor that takes items of tableview for initialization
	 * 
	 * @param tableView
	 */
	public TableFilter(TableView<S> tableView) {
		this.tableView = tableView;
		this.unfilteredItems.addAll(tableView.getItems());
		this.unfilteredItems.addListener((ListChangeListener<S>) changeevent -> {
			applyFilter();
		});
		// check if itemsProperty is bound when tableview is attached to scene
		this.tableView.sceneProperty().addListener((ChangeListener<Scene>) (observable, oldValue, newValue) -> checkItemsProperty());
	}

	/**
	 * Constructor with list for initialization
	 * 
	 * @param tableView
	 * @param unfilteredItems
	 */
	public TableFilter(TableView<S> tableView, List<S> unfilteredItems) {
		this(tableView);
		this.unfilteredItems.addAll(unfilteredItems);
	}
	
	private void checkItemsProperty()
	{
		boolean isBound = tableView.itemsProperty().isBound();
		if(isBound)
		{
			throw new RuntimeException("itemsProperty of TableView must not be bound! Use unfilteredItemsProperty for binding.");
		}
	}

	public <T extends Comparable<T>> void filterColumn(TableColumn<S, T> column) {
		ColumnFilter<S, T> filteredColumn = new ColumnFilter<S, T>(this, column);
		filteredColumn.blacklistedRowIndexesProperty()
				.addListener((ListChangeListener<Integer>) changeevent -> onBlacklistedRowIndexesChanged(changeevent,
						filteredColumn));
	}

	private void onBlacklistedRowIndexesChanged(Change<? extends Integer> changeEvent,
			ColumnFilter<S, ?> filteredColumn) {
		while (changeEvent.next()) {
			for (Integer addedIndex : changeEvent.getAddedSubList()) {
				List<ColumnFilter<S, ?>> blacklistingColumns = mapRowIndexToBlacklistingColumn.get(addedIndex);
				if (blacklistingColumns == null) {
					blacklistingColumns = new ArrayList<>();
					mapRowIndexToBlacklistingColumn.put(addedIndex, blacklistingColumns);
				}
				blacklistingColumns.add(filteredColumn);
			}
			for (Integer removedIndex : changeEvent.getRemoved()) {
				List<ColumnFilter<S, ?>> blacklistingColumns = mapRowIndexToBlacklistingColumn.get(removedIndex);
				if (blacklistingColumns != null) {
					blacklistingColumns.remove(filteredColumn);
					if (blacklistingColumns.isEmpty()) {
						mapRowIndexToBlacklistingColumn.remove(removedIndex);
					}
				}
			}
		}
	}

	private void applyFilter() {
		List<S> nextList = new ArrayList<>();
		for (int i = 0; i < unfilteredItems.size(); i++) {
			if (mapRowIndexToBlacklistingColumn.get(i) == null) {
				nextList.add(unfilteredItems.get(i));
			}
		}
		tableView.getItems().setAll(nextList);
	}

	public final ListProperty<S> unfilteredItemsProperty() {
		return this.unfilteredItems;
	}

	public final javafx.collections.ObservableList<S> getUnfilteredItems() {
		return this.unfilteredItemsProperty().get();
	}

	public final void setUnfilteredItems(final javafx.collections.ObservableList<S> unfilteredItems) {
		this.unfilteredItemsProperty().set(unfilteredItems);
	}

	public TableView<S> getTableView() {
		return tableView;
	}

}
