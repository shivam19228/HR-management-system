import { Component, OnInit } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HrDataService, AttendanceItem, EmployeeItem } from '../../services/hr-data';

@Component({
  selector: 'app-attendance',
  imports: [CommonModule, NgIf, NgFor, FormsModule],
  templateUrl: './attendance.html',
  styleUrl: './attendance.css'
})
export class Attendance implements OnInit {
  attendanceList: AttendanceItem[] = [];
  employees: EmployeeItem[] = [];

  searchTerm = '';
  statusFilter = 'All';

  showForm = false;
  isEditing = false;
  currentEditId: number | null = null;

  showViewModal = false;
  selectedAttendance: AttendanceItem | null = null;

  successMessage = '';
  errorMessage = '';

  attendanceForm = {
    employee: '',
    date: '',
    checkIn: '09:00 AM',
    checkOut: '05:30 PM',
    status: 'Present' as 'Present' | 'Absent' | 'Late' | 'Half Day',
    remarks: 'Regular attendance'
  };

  constructor(private hrData: HrDataService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.attendanceList = this.hrData.getAttendance();
    this.employees = this.hrData.getEmployees();
    this.hrData.fetchAttendance().subscribe({
      next: (a) => { if (a && a.length) this.attendanceList = a; }
    });
    this.hrData.fetchEmployees().subscribe({
      next: (e) => { if (e && e.length) this.employees = e; }
    });
  }

  get filteredAttendance(): AttendanceItem[] {
    return this.attendanceList.filter(item => {
      const q = this.searchTerm.toLowerCase();
      const matchesSearch = !this.searchTerm.trim() ||
        item.employee?.toLowerCase().includes(q) ||
        item.date?.toLowerCase().includes(q) ||
        item.status?.toLowerCase().includes(q) ||
        item.remarks?.toLowerCase().includes(q);

      const matchesStatus = this.statusFilter === 'All' || item.status === this.statusFilter;

      return matchesSearch && matchesStatus;
    });
  }

  openAddModal(): void {
    this.isEditing = false;
    this.currentEditId = null;
    this.attendanceForm = {
      employee: this.employees.length > 0 ? this.employees[0].name : 'David Chen',
      date: new Date().toISOString().split('T')[0],
      checkIn: '09:00 AM',
      checkOut: '05:30 PM',
      status: 'Present',
      remarks: 'On time arrival'
    };
    this.showForm = true;
    this.clearAlerts();
  }

  openEditModal(item: AttendanceItem): void {
    this.isEditing = true;
    this.currentEditId = item.id;
    this.attendanceForm = {
      employee: item.employee,
      date: item.date,
      checkIn: item.checkIn,
      checkOut: item.checkOut,
      status: item.status,
      remarks: item.remarks || ''
    };
    this.showForm = true;
    this.clearAlerts();
  }

  viewAttendance(item: AttendanceItem): void {
    this.selectedAttendance = item;
    this.showViewModal = true;
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.selectedAttendance = null;
  }

  saveAttendance(): void {
    this.clearAlerts();

    if (!this.attendanceForm.employee.trim() || !this.attendanceForm.date.trim()) {
      this.errorMessage = 'Employee and Date fields are required.';
      return;
    }

    if (this.isEditing && this.currentEditId) {
      this.hrData.updateAttendance(this.currentEditId, { ...this.attendanceForm });
      this.successMessage = `Attendance record for ${this.attendanceForm.employee} updated.`;
    } else {
      this.hrData.addAttendance({ ...this.attendanceForm });
      this.successMessage = `Attendance logged for ${this.attendanceForm.employee}.`;
    }

    this.loadData();
    this.showForm = false;
    setTimeout(() => this.clearAlerts(), 4000);
  }

  deleteAttendance(id: number): void {
    if (confirm('Delete this attendance entry?')) {
      this.hrData.deleteAttendance(id);
      this.loadData();
      this.successMessage = 'Attendance entry deleted.';
      setTimeout(() => this.clearAlerts(), 3000);
    }
  }

  clearAlerts(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }
}