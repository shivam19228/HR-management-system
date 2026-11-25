import { Component, OnInit } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HrDataService, DepartmentItem } from '../../services/hr-data';

@Component({
  selector: 'app-departments',
  imports: [CommonModule, NgIf, NgFor, FormsModule],
  templateUrl: './departments.html',
  styleUrl: './departments.css'
})
export class Departments implements OnInit {
  departments: DepartmentItem[] = [];
  searchTerm = '';

  showForm = false;
  isEditing = false;
  currentEditId: number | null = null;

  showViewModal = false;
  selectedDepartment: DepartmentItem | null = null;

  successMessage = '';
  errorMessage = '';

  departmentForm = {
    departmentId: '',
    name: '',
    code: '',
    description: '',
    manager: ''
  };

  constructor(private hrData: HrDataService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.departments = this.hrData.getDepartments();
    this.hrData.fetchDepartments().subscribe({
      next: (d) => { if (d && d.length) this.departments = d; }
    });
  }

  get filteredDepartments(): DepartmentItem[] {
    return this.departments.filter(dep => {
      if (!this.searchTerm.trim()) return true;
      const q = this.searchTerm.toLowerCase();
      return dep.name?.toLowerCase().includes(q) ||
             dep.code?.toLowerCase().includes(q) ||
             dep.departmentId?.toLowerCase().includes(q) ||
             dep.manager?.toLowerCase().includes(q) ||
             dep.description?.toLowerCase().includes(q);
    });
  }

  openAddModal(): void {
    this.isEditing = false;
    this.currentEditId = null;
    const nextId = this.departments.length + 1;
    this.departmentForm = {
      departmentId: `DEP-${nextId + 100}`,
      name: '',
      code: `DEP-0${nextId}`,
      description: '',
      manager: ''
    };
    this.showForm = true;
    this.clearAlerts();
  }

  openEditModal(dep: DepartmentItem): void {
    this.isEditing = true;
    this.currentEditId = dep.id;
    this.departmentForm = {
      departmentId: dep.departmentId,
      name: dep.name,
      code: dep.code,
      description: dep.description,
      manager: dep.manager
    };
    this.showForm = true;
    this.clearAlerts();
  }

  viewDepartment(dep: DepartmentItem): void {
    this.selectedDepartment = dep;
    this.showViewModal = true;
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.selectedDepartment = null;
  }

  saveDepartment(): void {
    this.clearAlerts();

    if (!this.departmentForm.name.trim() || !this.departmentForm.code.trim()) {
      this.errorMessage = 'Please provide both department name and code.';
      return;
    }

    if (this.isEditing && this.currentEditId) {
      this.hrData.updateDepartment(this.currentEditId, { ...this.departmentForm });
      this.successMessage = `Department "${this.departmentForm.name}" updated successfully.`;
    } else {
      this.hrData.addDepartment({ ...this.departmentForm });
      this.successMessage = `Department "${this.departmentForm.name}" added successfully.`;
    }

    this.loadData();
    this.showForm = false;
    setTimeout(() => this.clearAlerts(), 4000);
  }

  deleteDepartment(id: number, name: string): void {
    if (confirm(`Are you sure you want to delete the department "${name}"?`)) {
      this.hrData.deleteDepartment(id);
      this.loadData();
      this.successMessage = `Department "${name}" removed.`;
      setTimeout(() => this.clearAlerts(), 3000);
    }
  }

  clearAlerts(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }
}