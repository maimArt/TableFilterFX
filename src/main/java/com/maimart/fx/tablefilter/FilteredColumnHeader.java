package com.maimart.fx.tablefilter;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Custom header for filtered columns
 * 
 * @author maim
 *
 */
public class FilteredColumnHeader extends HBox {

	private final BooleanProperty isFiltered = new SimpleBooleanProperty(false);
	private final Image isFilteredIcon = new Image(
			FilteredColumnHeader.class.getResourceAsStream("/images/filtered.png"));
	private final Image isNotFilteredIcon = new Image(
			FilteredColumnHeader.class.getResourceAsStream("/images/not_filtered.png"));
	private final ImageView buttonIcon = new ImageView();

	private Label label;
	private Button filterButton;

	public FilteredColumnHeader(TableColumn<?, ?> column) {
		super();
		// init nodes
		this.prefWidthProperty().bind(column.widthProperty());
		if (column.isSortable()) {
			setPadding(new Insets(0, 30, 0, 0));
		}
		this.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
		this.setAlignment(Pos.CENTER_LEFT);
		this.getStyleClass().add("filteredColumnHeader");
		this.label = new Label();
		label.textProperty().bind(column.textProperty());
		this.label.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(this.label, Priority.ALWAYS);
		this.filterButton = buildFilterButton();
		this.getChildren().addAll(this.label, filterButton);

		// handle button icon
		buttonIcon.setImage(isNotFilteredIcon);
		isFiltered.addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
			if (newValue) {
				buttonIcon.setImage(isFilteredIcon);
			} else {
				buttonIcon.setImage(isNotFilteredIcon);
			}
		});

	}

	private Button buildFilterButton() {
		Button filterButton = new Button();
		filterButton.setGraphic(buttonIcon);
		filterButton.setFocusTraversable(false);
		return filterButton;
	}

	public Label getLabel() {
		return label;
	}

	public Button getFilterButton() {
		return filterButton;
	}

	public final BooleanProperty isFilteredProperty() {
		return this.isFiltered;
	}

	public final boolean isIsFiltered() {
		return this.isFilteredProperty().get();
	}

	public final void setIsFiltered(final boolean isFiltered) {
		this.isFilteredProperty().set(isFiltered);
	}

}
