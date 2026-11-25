import { Component, OnInit } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HrDataService, LeaveItem, EmployeeItem } from '../../services/hr-data';

@Component({
  selector: 'app-leaves',
  imports: [CommonModule, NgIf, NgFor, FormsModule],
  templateUrl: './leaves.html',
  styleUrl: './leaves.css'
})
export class Leaves implements OnInit {
  leaves: LeaveItem[] = [];
  employees: EmployeeItem[] = [];

  searchTerm = '';
  statusFilter = 'All';

  showForm = false;
  isEditing = false;
  currentEditId: number | null = null;

  showViewModal = false;
  selectedLeave: LeaveItem | null = null;

  successMessage = '';
  errorMessage = '';

  leaveForm = {
    employee: '',
    leaveType: 'Casual Leave' as 'Casual Leave' | 'Sick Leave' | 'Earned Leave' | 'Emergency Leave' | 'Maternity Leave',
    startDate: '',
    endDate: '',
    totalDays: 1,
    reason: '',
    status: 'Pending' as 'Pending' | 'Approved' | 'Rejected'
  };

  constructor(private hrData: HrDataService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.leaves = this.hrData.getLeaves();
    this.employees = this.hrData.getEmployees();
    this.hrData.fetchLeaves().subscribe({
      next: (l) => { if (l && l.length) this.leaves = l; }
    });
    this.hrData.fetchEmployees().subscribe({
      next: (e) => { if (e && e.length) this.employees = e; }
    });
  }

  get filteredLeaves(): LeaveItem[] {
    return this.leaves.filter(l => {
      const q = this.searchTerm.toLowerCase();
      const matchesSearch = !this.searchTerm.trim() ||
        l.employee?.toLowerCase().includes(q) ||
        l.leaveType?.toLowerCase().includes(q) ||
        l.reason?.toLowerCase().includes(q) ||
        l.startDate?.toLowerCase().includes(q);

      const matchesStatus = this.statusFilter === 'All' || l.status === this.statusFilter;

      return matchesSearch && matchesStatus;
    });
  }

  openAddModal(): void {
    this.isEditing = false;
    this.currentEditId = null;
    const today = new Date().toISOString().split('T')[0];
    this.leaveForm = {
      employee: this.employees.length > 0 ? this.employees[0].name : 'David Chen',
      leaveType: 'Casual Leave',
      startDate: today,
      endDate: today,
      totalDays: 1,
      reason: '',
      status: 'Pending'
    };
    this.showForm = true;
    this.clearAlerts();
  }

  openEditModal(l: LeaveItem): void {
    this.isEditing = true;
    this.currentEditId = l.id;
    this.leaveForm = {
      employee: l.employee,
      leaveType: l.leaveType,
      startDate: l.startDate,
      endDate: l.endDate,
      totalDays: l.totalDays,
      reason: l.reason,
      status: l.status
    };
    this.showForm = true;
    this.clearAlerts();
  }

  viewLeave(l: LeaveItem): void {
    this.selectedLeave = l;
    this.showViewModal = true;
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.selectedLeave = null;
  }

  calculateDays(): void {
    if (this.leaveForm.startDate && this.leaveForm.endDate) {
      const start = new Date(this.leaveForm.startDate);
      const end = new Date(this.leaveForm.endDate);
      const diffTime = end.getTime() - start.getTime();
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
      this.leaveForm.totalDays = diffDays > 0 ? diffDays : 1;
    }
  }

  saveLeave(): void {
    this.clearAlerts();

    if (!this.leaveForm.employee.trim() || !this.leaveForm.reason.trim()) {
      this.errorMessage = 'Please provide Employee and Reason for leave.';
      return;
    }

    if (this.isEditing && this.currentEditId) {
      this.hrData.updateLeave(this.currentEditId, { ...this.leaveForm });
      this.successMessage = `Leave application for ${this.leaveForm.employee} updated.`;
    } else {
      this.hrData.addLeave({ ...this.leaveForm });
      this.successMessage = `Leave applied for ${this.leaveForm.employee}.`;
    }

    this.loadData();
    this.showForm = false;
    setTimeout(() => this.clearAlerts(), 4000);
  }

  updateStatus(l: LeaveItem, newStatus: 'Approved' | 'Rejected'): void {
    this.hrData.updateLeave(l.id, { status: newStatus });
    this.loadData();
    this.successMessage = `Leave request for ${l.employee} marked as ${newStatus}.`;
    setTimeout(() => this.clearAlerts(), 3000);
  }

  deleteLeave(id: number): void {
    if (confirm('Delete this leave application?')) {
      this.hrData.deleteLeave(id);
      this.loadData();
      this.successMessage = 'Leave application removed.';
      setTimeout(() => this.clearAlerts(), 3000);
    }
  }

  clearAlerts(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }
}
