package com.example.SeniorProject.DTOs;

public class ProductDTO
{
	/*    public Product(int quantity, double price, String type, String name, String description, String location) {
	 */
	private int id;
	private String name;
	private double price;
	private double deposit;
	private int quantity;
	private String type;
	private String description;

	public ProductDTO()
	{

	}

	public ProductDTO(int id, String name, double price)
	{
		this.id = id;
		this.name = name;
		this.price = price;
		this.deposit = this.price / 2;
	}

	public ProductDTO(int id, String name, double price, String type)
	{
		this(id, name, price);
		this.type = type;
	}

	public ProductDTO(int id, String name, double price, int quantity, String type, String description)
	{
		this(id,name,price, type);
		this.quantity = quantity;
		this.description = description;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public double getPrice()
	{
		return price;
	}

	public void setPrice(double price)
	{
		this.price = price;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public void setQuantity(int quantity)
	{
		this.quantity = quantity;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public double getDeposit()
	{
		return deposit;
	}

	public void setDeposit(double deposit)
	{
		this.deposit = deposit;
	}
}