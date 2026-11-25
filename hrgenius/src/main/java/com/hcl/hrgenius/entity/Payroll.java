package com.hcl.hrgenius.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payrolls")
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee", nullable = false, length = 150)
    private String employee;

    @Column(name = "employee_id", length = 50)
    private String employeeId;

    @Column(name = "salary", precision = 12, scale = 2, nullable = false)
    private BigDecimal salary = BigDecimal.ZERO;

    @Column(name = "allowances", precision = 12, scale = 2)
    private BigDecimal allowances = BigDecimal.ZERO;

    @Column(name = "deductions", precision = 12, scale = 2)
    private BigDecimal deductions = BigDecimal.ZERO;

    @Column(name = "bonus", precision = 12, scale = 2)
    private BigDecimal bonus = BigDecimal.ZERO;

    @Column(name = "net_salary", precision = 12, scale = 2, nullable = false)
    private BigDecimal netSalary = BigDecimal.ZERO;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "pay_month", length = 100)
    private String payMonth;

    @Column(name = "status", length = 50)
    private String status = "Paid";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Payroll() {
    }

    public Payroll(String employee, String employeeId, BigDecimal salary, BigDecimal allowances,
                   BigDecimal deductions, BigDecimal bonus, BigDecimal netSalary,
                   LocalDate paymentDate, String payMonth, String status) {
        this.employee = employee;
        this.employeeId = employeeId;
        this.salary = salary != null ? salary : BigDecimal.ZERO;
        this.allowances = allowances != null ? allowances : BigDecimal.ZERO;
        this.deductions = deductions != null ? deductions : BigDecimal.ZERO;
        this.bonus = bonus != null ? bonus : BigDecimal.ZERO;
        this.netSalary = netSalary != null ? netSalary : this.salary.add(this.allowances).add(this.bonus).subtract(this.deductions);
        this.paymentDate = paymentDate != null ? paymentDate : LocalDate.now();
        this.payMonth = payMonth;
        this.status = status != null ? status : "Paid";
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        calculateNetSalary();
        if (this.paymentDate == null) {
            this.paymentDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateNetSalary();
    }

    public void calculateNetSalary() {
        BigDecimal base = this.salary != null ? this.salary : BigDecimal.ZERO;
        BigDecimal allow = this.allowances != null ? this.allowances : BigDecimal.ZERO;
        BigDecimal bon = this.bonus != null ? this.bonus : BigDecimal.ZERO;
        BigDecimal ded = this.deductions != null ? this.deductions : BigDecimal.ZERO;
        this.netSalary = base.add(allow).add(bon).subtract(ded);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public BigDecimal getAllowances() {
        return allowances;
    }

    public void setAllowances(BigDecimal allowances) {
        this.allowances = allowances;
    }

    public BigDecimal getDeductions() {
        return deductions;
    }

    public void setDeductions(BigDecimal deductions) {
        this.deductions = deductions;
    }

    public BigDecimal getBonus() {
        return bonus;
    }

    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }

    public BigDecimal getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(BigDecimal netSalary) {
        this.netSalary = netSalary;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPayMonth() {
        return payMonth;
    }

    public void setPayMonth(String payMonth) {
        this.payMonth = payMonth;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
