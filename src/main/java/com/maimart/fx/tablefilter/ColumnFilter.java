package com.maimart.fx.tablefilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.maimart.fx.tablefilter.popup.FilterPopUp;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.input.MouseEvent;

/**
 * Filtered column
 * 
 * @author maim
 *
 * @param <S>
 * @param <T>
 */
public class ColumnFilter<S, T extends Comparable<T>> {

	private final TableFilter<S> tableFilter;
	private final TableColumn<S, T> column;
	private final FilteredColumnHeader header;
	private final FilterPopUp<T> filterPopup;
	private final ListProperty<T> allItemsMerged = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final ListProperty<T> blacklistedItems = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final ListProperty<Integer> blacklistedRowIndexes = new SimpleListProperty<>(
			FXCollections.observableArrayList());
	private Map<T, List<Integer>> mapItemToRowIndexes = new HashMap<>();

	public ColumnFilter(TableFilter<S> tablefilter, TableColumn<S, T> columnToFilter) {
		super();
		this.tableFilter = tablefilter;
		this.column = columnToFilter;
		EventHandler<CellEditEvent<S, T>> editCommitHandler = this.column.getOnEditCommit();
		this.column.setOnEditCommit(event -> {
			editCommitHandler.handle(event);
			updateItems();			
		});

		// set custom header
		header = new FilteredColumnHeader(column);
		header.prefWidthProperty().bind(column.widthProperty());
		header.getFilterButton().setOnMouseClicked(this::onFilterButtonClicked);
		header.isFilteredProperty().bind(blacklistedItems.emptyProperty().not());
		column.setGraphic(header);

		filterPopup = new FilterPopUp<T>(allItemsMerged, blacklistedItems);
		blacklistedItems.addListener(this::onBlacklistedItemsChanged);
		initColumnFilter();
	}

	/**
	 * Callback when column is attached to tableview
	 * 
	 * @param observable
	 * @param oldValue
	 * @param newValue
	 */
	private void initColumnFilter() {
		filterPopup.getRootContent().maxHeightProperty()
				.bind(tableFilter.getTableView().heightProperty().multiply(0.5));

		filterPopup.getRootContent().maxWidthProperty().bind(column.widthProperty());
		filterPopup.getRootContent().prefWidthProperty().bind(column.widthProperty());
		updateItems();
		tableFilter.unfilteredItemsProperty().addListener((ListChangeListener<S>) changeevent -> {
			updateItems();
		});
	}

	private void updateItems() {
		mapIndexesToItems();
		updateMergedCellValues();
	}

	/**
	 * Callback when blacklistitems changed
	 * 
	 * @param changeEvent
	 */
	private void onBlacklistedItemsChanged(Change<? extends T> changeEvent) {
		List<Integer> addedIndexes = new ArrayList<>();
		List<Integer> removedIndexes = new ArrayList<>();
		while (changeEvent.next()) {
			for (T addedItem : changeEvent.getAddedSubList()) {
				addedIndexes.addAll(mapItemToRowIndexes.get(addedItem));
			}
			for (T removedItem : changeEvent.getRemoved()) {
				removedIndexes.addAll(mapItemToRowIndexes.get(removedItem));
			}
		}
		blacklistedRowIndexes.addAll(addedIndexes);
		blacklistedRowIndexes.removeAll(removedIndexes);
	}

	private void onFilterButtonClicked(MouseEvent event) {
		if (!filterPopup.isShowing()) {
			Bounds bounds = header.getBoundsInLocal();
			Bounds screenBounds = header.localToScreen(bounds);
			filterPopup.show(header, screenBounds.getMinX(), screenBounds.getMaxY());
		}
		event.consume();
	}

	/**
	 * update mergededCells by all cell values
	 */
	private void updateMergedCellValues() {
		List<T> mergedValues = new ArrayList<>();
		for (T value : mapItemToRowIndexes.keySet()) {
			boolean alreadyExists = false;
			for (T existingValue : mergedValues) {
				if (existingValue == null && value == null) {
					alreadyExists = true;
				} else if (existingValue != null && existingValue.compareTo(value) == 0) {
					alreadyExists = true;
					break;
				}

			}
			if (!alreadyExists) {
				mergedValues.add(value);
			}
		}
		Collections.sort(mergedValues);
		allItemsMerged.setAll(mergedValues);
	}

	/**
	 * map indexs of the items to the items
	 */
	private void mapIndexesToItems() {
		mapItemToRowIndexes.clear();
		for (int i = 0; i < tableFilter.getUnfilteredItems().size(); i++) {
			T item = column.getCellData(i);
			List<Integer> indexes = mapItemToRowIndexes.get(item);
			if (indexes == null) {
				indexes = new ArrayList<>();
				mapItemToRowIndexes.put(item, indexes);
			}
			indexes.add(i);
		}
	}

	public final ListProperty<Integer> blacklistedRowIndexesProperty() {
		return this.blacklistedRowIndexes;
	}

	public final javafx.collections.ObservableList<java.lang.Integer> getBlacklistedRowIndexes() {
		return this.blacklistedRowIndexesProperty().get();
	}

}
