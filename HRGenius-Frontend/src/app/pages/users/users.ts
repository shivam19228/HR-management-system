import { Component, OnInit } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HrDataService, UserItem, EmployeeItem } from '../../services/hr-data';

@Component({
  selector: 'app-users',
  imports: [CommonModule, NgIf, NgFor, FormsModule],
  templateUrl: './users.html',
  styleUrl: './users.css'
})
export class Users implements OnInit {
  users: UserItem[] = [];
  employees: EmployeeItem[] = [];

  searchTerm = '';
  roleFilter = 'All';

  showForm = false;
  isEditing = false;
  currentEditId: number | null = null;

  showViewModal = false;
  selectedUser: UserItem | null = null;

  successMessage = '';
  errorMessage = '';

  userForm = {
    username: '',
    employee: '',
    role: 'HR Manager' as 'Admin' | 'HR Manager' | 'Recruiter' | 'Employee',
    status: 'Active' as 'Active' | 'Inactive',
    email: ''
  };

  constructor(private hrData: HrDataService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.users = this.hrData.getUsers();
    this.employees = this.hrData.getEmployees();
    this.hrData.fetchUsers().subscribe({
      next: (u) => { if (u && u.length) this.users = u; }
    });
    this.hrData.fetchEmployees().subscribe({
      next: (e) => { if (e && e.length) this.employees = e; }
    });
  }

  get filteredUsers(): UserItem[] {
    return this.users.filter(u => {
      const q = this.searchTerm.toLowerCase();
      const matchesSearch = !this.searchTerm.trim() ||
        u.username?.toLowerCase().includes(q) ||
        u.employee?.toLowerCase().includes(q) ||
        u.email?.toLowerCase().includes(q) ||
        u.role?.toLowerCase().includes(q);

      const matchesRole = this.roleFilter === 'All' || u.role === this.roleFilter;

      return matchesSearch && matchesRole;
    });
  }

  openAddModal(): void {
    this.isEditing = false;
    this.currentEditId = null;
    const defaultEmp = this.employees.length > 0 ? this.employees[0].name : 'David Chen';
    this.userForm = {
      username: '',
      employee: defaultEmp,
      role: 'HR Manager',
      status: 'Active',
      email: ''
    };
    this.showForm = true;
    this.clearAlerts();
  }

  openEditModal(u: UserItem): void {
    this.isEditing = true;
    this.currentEditId = u.id;
    this.userForm = {
      username: u.username,
      employee: u.employee,
      role: u.role,
      status: u.status,
      email: u.email
    };
    this.showForm = true;
    this.clearAlerts();
  }

  viewUser(u: UserItem): void {
    this.selectedUser = u;
    this.showViewModal = true;
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.selectedUser = null;
  }

  saveUser(): void {
    this.clearAlerts();

    if (!this.userForm.username.trim() || !this.userForm.employee.trim()) {
      this.errorMessage = 'Please provide both Username and associated Employee.';
      return;
    }

    if (!this.userForm.email.trim()) {
      this.userForm.email = `${this.userForm.username.toLowerCase()}@hrgenius.com`;
    }

    if (this.isEditing && this.currentEditId) {
      this.hrData.updateUser(this.currentEditId, { ...this.userForm });
      this.successMessage = `User account "${this.userForm.username}" updated.`;
    } else {
      this.hrData.addUser({ ...this.userForm });
      this.successMessage = `User account "${this.userForm.username}" created!`;
    }

    this.loadData();
    this.showForm = false;
    setTimeout(() => this.clearAlerts(), 4000);
  }

  deleteUser(id: number, username: string): void {
    if (confirm(`Revoke and delete user access for "${username}"?`)) {
      this.hrData.deleteUser(id);
      this.loadData();
      this.successMessage = `User "${username}" deleted.`;
      setTimeout(() => this.clearAlerts(), 3000);
    }
  }

  clearAlerts(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }
}
