import { Component, OnInit } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HrDataService, TrainingItem, EmployeeItem } from '../../services/hr-data';

@Component({
  selector: 'app-training',
  imports: [CommonModule, NgIf, NgFor, FormsModule],
  templateUrl: './training.html',
  styleUrl: './training.css'
})
export class Training implements OnInit {
  trainings: TrainingItem[] = [];
  employees: EmployeeItem[] = [];

  searchTerm = '';
  statusFilter = 'All';

  showForm = false;
  isEditing = false;
  currentEditId: number | null = null;

  showViewModal = false;
  selectedTraining: TrainingItem | null = null;

  successMessage = '';
  errorMessage = '';

  trainingForm = {
    employee: '',
    trainingName: '',
    trainer: '',
    startDate: '',
    endDate: '',
    status: 'Upcoming' as 'Upcoming' | 'In Progress' | 'Completed'
  };

  constructor(private hrData: HrDataService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.trainings = this.hrData.getTrainings();
    this.employees = this.hrData.getEmployees();
    this.hrData.fetchTrainings().subscribe({
      next: (t) => { if (t && t.length) this.trainings = t; }
    });
    this.hrData.fetchEmployees().subscribe({
      next: (e) => { if (e && e.length) this.employees = e; }
    });
  }

  get filteredTrainings(): TrainingItem[] {
    return this.trainings.filter(t => {
      const q = this.searchTerm.toLowerCase();
      const matchesSearch = !this.searchTerm.trim() ||
        t.employee?.toLowerCase().includes(q) ||
        t.trainingName?.toLowerCase().includes(q) ||
        t.trainer?.toLowerCase().includes(q);

      const matchesStatus = this.statusFilter === 'All' || t.status === this.statusFilter;

      return matchesSearch && matchesStatus;
    });
  }

  openAddModal(): void {
    this.isEditing = false;
    this.currentEditId = null;
    const start = new Date().toISOString().split('T')[0];
    const end = new Date(Date.now() + 14 * 86400000).toISOString().split('T')[0];
    this.trainingForm = {
      employee: this.employees.length > 0 ? this.employees[0].name : 'David Chen',
      trainingName: '',
      trainer: 'Executive Technical Coach',
      startDate: start,
      endDate: end,
      status: 'Upcoming'
    };
    this.showForm = true;
    this.clearAlerts();
  }

  openEditModal(t: TrainingItem): void {
    this.isEditing = true;
    this.currentEditId = t.id;
    this.trainingForm = {
      employee: t.employee,
      trainingName: t.trainingName,
      trainer: t.trainer,
      startDate: t.startDate,
      endDate: t.endDate,
      status: t.status
    };
    this.showForm = true;
    this.clearAlerts();
  }

  viewTraining(t: TrainingItem): void {
    this.selectedTraining = t;
    this.showViewModal = true;
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.selectedTraining = null;
  }

  saveTraining(): void {
    this.clearAlerts();

    if (!this.trainingForm.employee.trim() || !this.trainingForm.trainingName.trim() || !this.trainingForm.trainer.trim()) {
      this.errorMessage = 'Please complete Employee, Training Name, and Trainer fields.';
      return;
    }

    if (this.isEditing && this.currentEditId) {
      this.hrData.updateTraining(this.currentEditId, { ...this.trainingForm });
      this.successMessage = `Training course for ${this.trainingForm.employee} updated.`;
    } else {
      this.hrData.addTraining({ ...this.trainingForm });
      this.successMessage = `Training course assigned to ${this.trainingForm.employee}!`;
    }

    this.loadData();
    this.showForm = false;
    setTimeout(() => this.clearAlerts(), 4000);
  }

  deleteTraining(id: number): void {
    if (confirm('Delete this training course record?')) {
      this.hrData.deleteTraining(id);
      this.loadData();
      this.successMessage = 'Training record removed.';
      setTimeout(() => this.clearAlerts(), 3000);
    }
  }

  clearAlerts(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }
}
