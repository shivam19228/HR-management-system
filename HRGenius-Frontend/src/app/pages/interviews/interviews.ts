import { Component, OnInit } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HrDataService, InterviewItem, CandidateItem, JobItem, EmployeeItem } from '../../services/hr-data';

@Component({
  selector: 'app-interviews',
  imports: [CommonModule, NgIf, NgFor, FormsModule],
  templateUrl: './interviews.html',
  styleUrl: './interviews.css'
})
export class Interviews implements OnInit {
  interviews: InterviewItem[] = [];
  candidates: CandidateItem[] = [];
  jobs: JobItem[] = [];
  employees: EmployeeItem[] = [];

  searchTerm = '';
  statusFilter = 'All';

  showForm = false;
  isEditing = false;
  currentEditId: number | null = null;

  showViewModal = false;
  selectedInterview: InterviewItem | null = null;

  successMessage = '';
  errorMessage = '';

  interviewForm = {
    candidate: '',
    job: '',
    interviewDate: '',
    interviewer: '',
    mode: 'Online' as 'Online' | 'In-Person',
    status: 'Scheduled' as 'Scheduled' | 'Completed' | 'Cancelled',
    feedback: ''
  };

  constructor(private hrData: HrDataService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.interviews = this.hrData.getInterviews();
    this.candidates = this.hrData.getCandidates();
    this.jobs = this.hrData.getJobs();
    this.employees = this.hrData.getEmployees();
    this.hrData.fetchInterviews().subscribe({
      next: (i) => { if (i && i.length) this.interviews = i; }
    });
    this.hrData.fetchCandidates().subscribe({
      next: (c) => { if (c && c.length) this.candidates = c; }
    });
    this.hrData.fetchJobs().subscribe({
      next: (j) => { if (j && j.length) this.jobs = j; }
    });
  }

  get filteredInterviews(): InterviewItem[] {
    return this.interviews.filter(i => {
      const q = this.searchTerm.toLowerCase();
      const matchesSearch = !this.searchTerm.trim() ||
        i.candidate?.toLowerCase().includes(q) ||
        i.job?.toLowerCase().includes(q) ||
        i.interviewer?.toLowerCase().includes(q) ||
        i.feedback?.toLowerCase().includes(q);

      const matchesStatus = this.statusFilter === 'All' || i.status === this.statusFilter;

      return matchesSearch && matchesStatus;
    });
  }

  openAddModal(): void {
    this.isEditing = false;
    this.currentEditId = null;
    const defaultDate = `${new Date().toISOString().split('T')[0]} 14:00`;
    this.interviewForm = {
      candidate: this.candidates.length > 0 ? this.candidates[0].name : 'Alex Morgan',
      job: this.jobs.length > 0 ? this.jobs[0].jobTitle : 'Senior Full Stack Engineer',
      interviewDate: defaultDate,
      interviewer: this.employees.length > 0 ? this.employees[0].name : 'Sarah Jenkins',
      mode: 'Online',
      status: 'Scheduled',
      feedback: 'Initial technical and behavioral assessment.'
    };
    this.showForm = true;
    this.clearAlerts();
  }

  openEditModal(i: InterviewItem): void {
    this.isEditing = true;
    this.currentEditId = i.id;
    this.interviewForm = {
      candidate: i.candidate,
      job: i.job,
      interviewDate: i.interviewDate,
      interviewer: i.interviewer,
      mode: i.mode,
      status: i.status,
      feedback: i.feedback
    };
    this.showForm = true;
    this.clearAlerts();
  }

  viewInterview(i: InterviewItem): void {
    this.selectedInterview = i;
    this.showViewModal = true;
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.selectedInterview = null;
  }

  saveInterview(): void {
    this.clearAlerts();

    if (!this.interviewForm.candidate.trim() || !this.interviewForm.job.trim() || !this.interviewForm.interviewDate.trim()) {
      this.errorMessage = 'Please specify Candidate, Job, and Interview Date/Time.';
      return;
    }

    if (this.isEditing && this.currentEditId) {
      this.hrData.updateInterview(this.currentEditId, { ...this.interviewForm });
      this.successMessage = `Interview for ${this.interviewForm.candidate} updated.`;
    } else {
      this.hrData.addInterview({ ...this.interviewForm });
      this.successMessage = `Interview scheduled for ${this.interviewForm.candidate}!`;
    }

    this.loadData();
    this.showForm = false;
    setTimeout(() => this.clearAlerts(), 4000);
  }

  deleteInterview(id: number): void {
    if (confirm('Delete this interview record?')) {
      this.hrData.deleteInterview(id);
      this.loadData();
      this.successMessage = 'Interview record deleted.';
      setTimeout(() => this.clearAlerts(), 3000);
    }
  }

  clearAlerts(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }
}
