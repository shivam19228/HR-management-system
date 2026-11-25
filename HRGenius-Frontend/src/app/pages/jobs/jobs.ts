import { Component, OnInit } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HrDataService, JobItem, DepartmentItem } from '../../services/hr-data';

@Component({
  selector: 'app-jobs',
  imports: [CommonModule, NgIf, NgFor, FormsModule],
  templateUrl: './jobs.html',
  styleUrl: './jobs.css'
})
export class Jobs implements OnInit {
  jobs: JobItem[] = [];
  departments: DepartmentItem[] = [];

  searchTerm = '';
  statusFilter = 'All';

  showForm = false;
  isEditing = false;
  currentEditId: number | null = null;

  showViewModal = false;
  selectedJob: JobItem | null = null;

  successMessage = '';
  errorMessage = '';

  jobForm = {
    jobId: '',
    jobTitle: '',
    department: 'Software Engineering',
    description: '',
    status: 'Open' as 'Open' | 'Closed' | 'Draft',
    location: 'Remote',
    employmentType: 'Full Time',
    experienceRequired: '2+ Years',
    salary: '$70,000 - $85,000'
  };

  constructor(private hrData: HrDataService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.jobs = this.hrData.getJobs();
    this.departments = this.hrData.getDepartments();
    this.hrData.fetchJobs().subscribe({
      next: (j) => { if (j && j.length) this.jobs = j; }
    });
    this.hrData.fetchDepartments().subscribe({
      next: (d) => { if (d && d.length) this.departments = d; }
    });
  }

  get filteredJobs(): JobItem[] {
    return this.jobs.filter(job => {
      const q = this.searchTerm.toLowerCase();
      const matchesSearch = !this.searchTerm.trim() ||
        job.jobTitle?.toLowerCase().includes(q) ||
        job.jobId?.toLowerCase().includes(q) ||
        job.department?.toLowerCase().includes(q) ||
        job.description?.toLowerCase().includes(q);

      const matchesStatus = this.statusFilter === 'All' || job.status === this.statusFilter;

      return matchesSearch && matchesStatus;
    });
  }

  openAddModal(): void {
    this.isEditing = false;
    this.currentEditId = null;
    const nextId = this.jobs.length + 1;
    this.jobForm = {
      jobId: `JOB-${nextId + 200}`,
      jobTitle: '',
      department: this.departments.length > 0 ? this.departments[0].name : 'Software Engineering',
      description: '',
      status: 'Open',
      location: 'Hybrid',
      employmentType: 'Full Time',
      experienceRequired: '2+ Years',
      salary: '$75,000 - $90,000'
    };
    this.showForm = true;
    this.clearAlerts();
  }

  openEditModal(job: JobItem): void {
    this.isEditing = true;
    this.currentEditId = job.id;
    this.jobForm = {
      jobId: job.jobId,
      jobTitle: job.jobTitle,
      department: job.department,
      description: job.description,
      status: job.status,
      location: job.location || 'Hybrid',
      employmentType: job.employmentType || 'Full Time',
      experienceRequired: job.experienceRequired || '2+ Years',
      salary: job.salary || '$75,000 - $90,000'
    };
    this.showForm = true;
    this.clearAlerts();
  }

  viewJob(job: JobItem): void {
    this.selectedJob = job;
    this.showViewModal = true;
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.selectedJob = null;
  }

  saveJob(): void {
    this.clearAlerts();

    if (!this.jobForm.jobTitle.trim() || !this.jobForm.department.trim()) {
      this.errorMessage = 'Please provide Job Title and Department.';
      return;
    }

    if (this.isEditing && this.currentEditId) {
      this.hrData.updateJob(this.currentEditId, { ...this.jobForm });
      this.successMessage = `Job "${this.jobForm.jobTitle}" updated successfully!`;
    } else {
      this.hrData.addJob({ ...this.jobForm });
      this.successMessage = `New Job opening "${this.jobForm.jobTitle}" created!`;
    }

    this.loadData();
    this.showForm = false;
    setTimeout(() => this.clearAlerts(), 4000);
  }

  deleteJob(id: number, title: string): void {
    if (confirm(`Are you sure you want to delete job vacancy "${title}"?`)) {
      this.hrData.deleteJob(id);
      this.loadData();
      this.successMessage = `Job "${title}" deleted.`;
      setTimeout(() => this.clearAlerts(), 3000);
    }
  }

  clearAlerts(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }
}