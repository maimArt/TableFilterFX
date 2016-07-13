package com.maimart.fx.tablefilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.PopupWindow.AnchorLocation;

/**
 * Filtered column
 * 
 * @author maim
 *
 * @param <S>
 * @param <T>
 */
public class ColumnFilter<S, T> {

	private final TableFilter<S> tableFilter;
	private final TableColumn<S, T> column;
	private final FilteredColumnHeader header;
	private final FilterPopUp<T> filterPopup;
	private final ListProperty<T> mergedCellValues = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final ListProperty<T> blacklistedCellValues = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final ListProperty<Integer> blacklistedRowIndexes = new SimpleListProperty<>(
			FXCollections.observableArrayList());
	private Map<T, List<Integer>> mapCellValuesToRowIndexes = new HashMap<>();
	private final Comparator<T> comparator = (o1, o2) -> o1.toString().compareTo(o2.toString());;

	public ColumnFilter(TableFilter<S> tablefilter, TableColumn<S, T> columnToFilter) {
		super();
		this.tableFilter = tablefilter;
		this.column = columnToFilter;
		EventHandler<CellEditEvent<S, T>> editCommitHandler = this.column.getOnEditCommit();
		this.column.setOnEditCommit(event -> {
			editCommitHandler.handle(event);
			updateCellValues();
		});

		// set custom header
		header = new FilteredColumnHeader(column);
		header.prefWidthProperty().bind(column.widthProperty());
		header.getFilterButton().setOnMouseClicked(this::onFilterButtonClicked);
		header.isFilteredProperty().bind(blacklistedCellValues.emptyProperty().not());
		column.setGraphic(header);

		filterPopup = new FilterPopUp<>(mergedCellValues, blacklistedCellValues);
		blacklistedCellValues.addListener(this::onBlacklistedCellValuesChanged);
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
		filterPopup.getRootContent().maxWidthProperty().bind(tableFilter.getTableView().widthProperty());
		filterPopup.getRootContent().minWidthProperty().bind(column.widthProperty());
		updateCellValues();
		tableFilter.unfilteredItemsProperty().addListener((ListChangeListener<S>) changeevent -> {
			blacklistedCellValues.removeListener(this::onBlacklistedCellValuesChanged);
			blacklistedCellValues.clear();
			blacklistedRowIndexes.clear();
			updateCellValues();
			blacklistedCellValues.addListener(this::onBlacklistedCellValuesChanged);
		});
	}

	private void updateCellValues() {
		mapIndexesToCellValues();
		updateMergedCellValues();
	}	

	/**
	 * Callback when blacklistitems changed
	 * 
	 * @param changeEvent
	 */
	private void onBlacklistedCellValuesChanged(Change<? extends T> changeEvent) {
		List<Integer> addedIndexes = new ArrayList<>();
		List<Integer> removedIndexes = new ArrayList<>();
		while (changeEvent.next()) {
			for (T addedItem : changeEvent.getAddedSubList()) {
				addedIndexes.addAll(mapCellValuesToRowIndexes.get(addedItem));
			}
			for (T removedItem : changeEvent.getRemoved()) {
				removedIndexes.addAll(mapCellValuesToRowIndexes.get(removedItem));
			}
		}
		blacklistedRowIndexes.addAll(addedIndexes);
		blacklistedRowIndexes.removeAll(removedIndexes);
	}

	private void onFilterButtonClicked(MouseEvent event) {
		if (!filterPopup.isShowing()) {
			Bounds bounds = header.getBoundsInLocal();
			Bounds screenBounds = header.localToScreen(bounds);
			int lastColIndex = tableFilter.getTableView().getColumns().size() - 1;
			int thisColIndex = tableFilter.getTableView().getColumns().indexOf(column);
			double xpos;
			if (thisColIndex == lastColIndex) {
				filterPopup.setAnchorLocation(AnchorLocation.CONTENT_TOP_RIGHT);
				Bounds boundsTable = tableFilter.getTableView().getBoundsInLocal();
				Bounds screentToTable = tableFilter.getTableView().localToScreen(boundsTable);
				xpos = screentToTable.getMaxX();
			} else {
				filterPopup.setAnchorLocation(AnchorLocation.CONTENT_TOP_LEFT);
				xpos = screenBounds.getMinX();
			}
			// TODO hack to init with right width
			filterPopup.show(header.getScene().getWindow());
			filterPopup.hide();
			filterPopup.show(header, xpos, screenBounds.getMaxY());
		}
		event.consume();
	}

	/**
	 * update mergededCells by all cell values
	 */
	private void updateMergedCellValues() {
		List<T> mergedValues = new ArrayList<>();
		for (T value : mapCellValuesToRowIndexes.keySet()) {
			boolean alreadyExists = false;
			for (T existingValue : mergedValues) {
				if (existingValue == null && value == null) {
					alreadyExists = true;
				} else if (existingValue != null && comparator.compare(value, existingValue) == 0) {
					alreadyExists = true;
					break;
				}

			}
			if (!alreadyExists) {
				mergedValues.add(value);
			}
		}
		Collections.sort(mergedValues, comparator);
		mergedCellValues.setAll(mergedValues);
	}

	/**
	 * map indexs of the items to the items
	 */
	private void mapIndexesToCellValues() {
		mapCellValuesToRowIndexes.clear();
		for (int i = 0; i < tableFilter.getUnfilteredItems().size(); i++) {
			S item = tableFilter.getUnfilteredItems().get(i);
			T cellValue = column.getCellValueFactory().call(new CellDataFeatures<>(tableFilter.getTableView(), column, item)).getValue();			
			List<Integer> indexes = mapCellValuesToRowIndexes.get(cellValue);
			if (indexes == null) {
				indexes = new ArrayList<>();
				mapCellValuesToRowIndexes.put(cellValue, indexes);
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
