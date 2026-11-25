import { Component, OnInit } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HrDataService, PayrollItem, EmployeeItem } from '../../services/hr-data';

@Component({
  selector: 'app-payroll',
  imports: [CommonModule, NgIf, NgFor, FormsModule],
  templateUrl: './payroll.html',
  styleUrl: './payroll.css'
})
export class Payroll implements OnInit {
  payrolls: PayrollItem[] = [];
  employees: EmployeeItem[] = [];

  searchTerm = '';
  statusFilter = 'All';

  showForm = false;
  isEditing = false;
  currentEditId: number | null = null;

  showViewModal = false;
  selectedPayroll: PayrollItem | null = null;

  successMessage = '';
  errorMessage = '';

  payrollForm = {
    employee: '',
    salary: 6000,
    allowances: 500,
    deductions: 350,
    bonus: 300,
    paymentDate: '',
    payMonth: 'October 2026',
    status: 'Paid' as 'Paid' | 'Pending'
  };

  constructor(private hrData: HrDataService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.payrolls = this.hrData.getPayroll();
    this.employees = this.hrData.getEmployees();
    this.hrData.fetchPayroll().subscribe({
      next: (p) => { if (p && p.length) this.payrolls = p; }
    });
    this.hrData.fetchEmployees().subscribe({
      next: (e) => { if (e && e.length) this.employees = e; }
    });
  }

  get computedNetSalary(): number {
    const s = Number(this.payrollForm.salary) || 0;
    const a = Number(this.payrollForm.allowances) || 0;
    const b = Number(this.payrollForm.bonus) || 0;
    const d = Number(this.payrollForm.deductions) || 0;
    return s + a + b - d;
  }

  get filteredPayrolls(): PayrollItem[] {
    return this.payrolls.filter(p => {
      const q = this.searchTerm.toLowerCase();
      const matchesSearch = !this.searchTerm.trim() ||
        p.employee?.toLowerCase().includes(q) ||
        p.paymentDate?.toLowerCase().includes(q) ||
        p.payMonth?.toLowerCase().includes(q);

      const matchesStatus = this.statusFilter === 'All' || p.status === this.statusFilter;

      return matchesSearch && matchesStatus;
    });
  }

  openAddModal(): void {
    this.isEditing = false;
    this.currentEditId = null;
    this.payrollForm = {
      employee: this.employees.length > 0 ? this.employees[0].name : 'David Chen',
      salary: 6500,
      allowances: 600,
      deductions: 400,
      bonus: 350,
      paymentDate: new Date().toISOString().split('T')[0],
      payMonth: 'October 2026',
      status: 'Paid'
    };
    this.showForm = true;
    this.clearAlerts();
  }

  openEditModal(p: PayrollItem): void {
    this.isEditing = true;
    this.currentEditId = p.id;
    this.payrollForm = {
      employee: p.employee,
      salary: p.salary,
      allowances: p.allowances,
      deductions: p.deductions,
      bonus: p.bonus,
      paymentDate: p.paymentDate,
      payMonth: p.payMonth,
      status: p.status
    };
    this.showForm = true;
    this.clearAlerts();
  }

  viewPayroll(p: PayrollItem): void {
    this.selectedPayroll = p;
    this.showViewModal = true;
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.selectedPayroll = null;
  }

  savePayroll(): void {
    this.clearAlerts();

    if (!this.payrollForm.employee.trim() || !this.payrollForm.paymentDate.trim()) {
      this.errorMessage = 'Please select Employee and Payment Date.';
      return;
    }

    if (this.isEditing && this.currentEditId) {
      this.hrData.updatePayroll(this.currentEditId, { ...this.payrollForm });
      this.successMessage = `Payroll for ${this.payrollForm.employee} updated.`;
    } else {
      this.hrData.addPayroll({ ...this.payrollForm });
      this.successMessage = `Payroll processed for ${this.payrollForm.employee}!`;
    }

    this.loadData();
    this.showForm = false;
    setTimeout(() => this.clearAlerts(), 4000);
  }

  deletePayroll(id: number): void {
    if (confirm('Delete this payroll record?')) {
      this.hrData.deletePayroll(id);
      this.loadData();
      this.successMessage = 'Payroll record deleted.';
      setTimeout(() => this.clearAlerts(), 3000);
    }
  }

  clearAlerts(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }
}