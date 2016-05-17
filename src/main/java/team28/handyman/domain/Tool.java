package team28.handyman.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Tool implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final long TOOL_TYPE_POWERTOOL = 3;
	public static final int TOOL_TYPE_HAND = 1;
	public static final int TOOL_TYPE_CONSTRUCTION = 2;
	@NotNull
	private int toolNumber;
	@Size(min=1, max=32)
	@NotNull
	private String abbreviatedDescription;	
	@Size(min=1, max=512)
	@NotNull
	private String fullDescription;
	@NotNull
	private Integer rentalPrice;
	
	@NotNull
	private Integer purchasePrice;
	
	@NotNull
	private Integer depositPrice;
	@NotNull
	private int toolType;
	
	//list date can be null
	private Date listDate;
	//sold date can be null
	private Date soldDate;
	public Date getListDate() {
		return listDate;
	}
	public Tool setListDate(Date listDate) {
		this.listDate = listDate;
		return this;
	}
	public Date getSoldDate() {
		return soldDate;
	}
	public Tool setSoldDate(Date soldDate) {
		this.soldDate = soldDate;
		return this;
	}
	public Integer getSalePrice() {
		return salePrice;
	}
	public Tool setSalePrice(Integer salePrice) {
		this.salePrice = salePrice;
		return this;
	}
	
	private Integer salePrice;
	private List<String> accessories = new LinkedList<String>();
	public int getToolNumber() {
		return toolNumber;
	}
	public Tool setToolNumber(int toolNumber) {
		this.toolNumber = toolNumber;
		return this;
	}
	public String getAbbreviatedDescription() {
		return abbreviatedDescription;
	}
	public Tool setAbbreviatedDescription(String abbreviatedDescription) {
		this.abbreviatedDescription = abbreviatedDescription;
		return this;
	}
	public String getFullDescription() {
		return fullDescription;
	}
	public Tool setFullDescription(String fullDescription) {
		this.fullDescription = fullDescription;
		return this;
	}
	public Integer getRentalPrice() {
		return rentalPrice;
	}
	public Tool setRentalPrice(Integer rentalPrice) {
		this.rentalPrice = rentalPrice;
		return this;
	}
	public Integer getPurchasePrice() {
		return purchasePrice;
	}
	public Tool setPurchasePrice(Integer purchasePrice) {
		this.purchasePrice = purchasePrice;
		return this;
	}
	public Integer getDepositPrice() {
		return depositPrice;
	}
	public Tool setDepositPrice(Integer depositPrice) {
		this.depositPrice = depositPrice;
		return this;
	}
	public int getToolType() {
		return toolType;
	}
	public Tool setToolType(int toolType) {
		this.toolType = toolType;
		return this;
	}
	public List<String> getAccessories() {
		return accessories;
	}
	public void setAccessories(List<String> accessories) {
		this.accessories = accessories;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj != null && 
				   obj instanceof Tool) {
			Tool t = (Tool) obj;
			return t.getToolNumber() == 
					this.getToolNumber();
		} else {
			return false;
		}
	}
}
