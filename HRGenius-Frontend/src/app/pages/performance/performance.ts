import { Component, OnInit } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HrDataService, PerformanceItem, EmployeeItem } from '../../services/hr-data';

@Component({
  selector: 'app-performance',
  imports: [CommonModule, NgIf, NgFor, FormsModule],
  templateUrl: './performance.html',
  styleUrl: './performance.css'
})
export class Performance implements OnInit {
  performances: PerformanceItem[] = [];
  employees: EmployeeItem[] = [];

  searchTerm = '';
  ratingFilter = 'All';

  showForm = false;
  isEditing = false;
  currentEditId: number | null = null;

  showViewModal = false;
  selectedPerformance: PerformanceItem | null = null;

  successMessage = '';
  errorMessage = '';

  performanceForm = {
    employee: '',
    reviewDate: '',
    rating: 5,
    goals: '',
    achievements: '',
    feedback: ''
  };

  constructor(private hrData: HrDataService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.performances = this.hrData.getPerformance();
    this.employees = this.hrData.getEmployees();
    this.hrData.fetchPerformance().subscribe({
      next: (p) => { if (p && p.length) this.performances = p; }
    });
    this.hrData.fetchEmployees().subscribe({
      next: (e) => { if (e && e.length) this.employees = e; }
    });
  }

  get filteredPerformances(): PerformanceItem[] {
    return this.performances.filter(p => {
      const q = this.searchTerm.toLowerCase();
      const matchesSearch = !this.searchTerm.trim() ||
        p.employee?.toLowerCase().includes(q) ||
        p.goals?.toLowerCase().includes(q) ||
        p.achievements?.toLowerCase().includes(q) ||
        p.feedback?.toLowerCase().includes(q);

      const matchesRating = this.ratingFilter === 'All' || p.rating.toString() === this.ratingFilter;

      return matchesSearch && matchesRating;
    });
  }

  openAddModal(): void {
    this.isEditing = false;
    this.currentEditId = null;
    this.performanceForm = {
      employee: this.employees.length > 0 ? this.employees[0].name : 'David Chen',
      reviewDate: new Date().toISOString().split('T')[0],
      rating: 5,
      goals: '',
      achievements: '',
      feedback: ''
    };
    this.showForm = true;
    this.clearAlerts();
  }

  openEditModal(p: PerformanceItem): void {
    this.isEditing = true;
    this.currentEditId = p.id;
    this.performanceForm = {
      employee: p.employee,
      reviewDate: p.reviewDate,
      rating: p.rating,
      goals: p.goals,
      achievements: p.achievements,
      feedback: p.feedback
    };
    this.showForm = true;
    this.clearAlerts();
  }

  viewPerformance(p: PerformanceItem): void {
    this.selectedPerformance = p;
    this.showViewModal = true;
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.selectedPerformance = null;
  }

  savePerformance(): void {
    this.clearAlerts();

    if (!this.performanceForm.employee.trim() || !this.performanceForm.goals.trim() || !this.performanceForm.feedback.trim()) {
      this.errorMessage = 'Please complete Employee, Goals, and Feedback fields.';
      return;
    }

    if (this.isEditing && this.currentEditId) {
      this.hrData.updatePerformance(this.currentEditId, { ...this.performanceForm });
      this.successMessage = `Appraisal review for ${this.performanceForm.employee} updated!`;
    } else {
      this.hrData.addPerformance({ ...this.performanceForm });
      this.successMessage = `Performance review submitted for ${this.performanceForm.employee}!`;
    }

    this.loadData();
    this.showForm = false;
    setTimeout(() => this.clearAlerts(), 4000);
  }

  deletePerformance(id: number): void {
    if (confirm('Delete this performance review?')) {
      this.hrData.deletePerformance(id);
      this.loadData();
      this.successMessage = 'Performance review record deleted.';
      setTimeout(() => this.clearAlerts(), 3000);
    }
  }

  clearAlerts(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }
}
