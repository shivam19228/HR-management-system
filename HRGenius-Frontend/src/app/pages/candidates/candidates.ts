import { Component, OnInit } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HrDataService, CandidateItem, JobItem } from '../../services/hr-data';

@Component({
  selector: 'app-candidates',
  imports: [CommonModule, NgIf, NgFor, FormsModule],
  templateUrl: './candidates.html',
  styleUrl: './candidates.css'
})
export class Candidates implements OnInit {
  candidates: CandidateItem[] = [];
  jobs: JobItem[] = [];

  searchTerm = '';
  statusFilter = 'All';

  showForm = false;
  isEditing = false;
  currentEditId: number | null = null;

  showViewModal = false;
  selectedCandidate: CandidateItem | null = null;

  successMessage = '';
  errorMessage = '';

  candidateForm = {
    candidateId: '',
    name: '',
    email: '',
    phone: '',
    appliedJob: 'Senior Full Stack Engineer',
    status: 'Applied' as 'Applied' | 'Screening' | 'Interviewing' | 'Offered' | 'Rejected' | 'Hired',
    qualification: "Bachelor's Degree",
    experience: '2 years'
  };

  constructor(private hrData: HrDataService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.candidates = this.hrData.getCandidates();
    this.jobs = this.hrData.getJobs();
    this.hrData.fetchCandidates().subscribe({
      next: (c) => { if (c && c.length) this.candidates = c; }
    });
    this.hrData.fetchJobs().subscribe({
      next: (j) => { if (j && j.length) this.jobs = j; }
    });
  }

  get filteredCandidates(): CandidateItem[] {
    return this.candidates.filter(c => {
      const q = this.searchTerm.toLowerCase();
      const matchesSearch = !this.searchTerm.trim() ||
        c.name?.toLowerCase().includes(q) ||
        c.candidateId?.toLowerCase().includes(q) ||
        c.email?.toLowerCase().includes(q) ||
        c.phone?.toLowerCase().includes(q) ||
        c.appliedJob?.toLowerCase().includes(q);

      const matchesStatus = this.statusFilter === 'All' || c.status === this.statusFilter;

      return matchesSearch && matchesStatus;
    });
  }

  openAddModal(): void {
    this.isEditing = false;
    this.currentEditId = null;
    const nextId = this.candidates.length + 1;
    this.candidateForm = {
      candidateId: `CAN-${nextId + 300}`,
      name: '',
      email: '',
      phone: '',
      appliedJob: this.jobs.length > 0 ? this.jobs[0].jobTitle : 'Senior Full Stack Engineer',
      status: 'Applied',
      qualification: "Bachelor's Degree",
      experience: '2 years'
    };
    this.showForm = true;
    this.clearAlerts();
  }

  openEditModal(can: CandidateItem): void {
    this.isEditing = true;
    this.currentEditId = can.id;
    this.candidateForm = {
      candidateId: can.candidateId,
      name: can.name,
      email: can.email,
      phone: can.phone,
      appliedJob: can.appliedJob,
      status: can.status,
      qualification: can.qualification || "Bachelor's Degree",
      experience: can.experience || '2 years'
    };
    this.showForm = true;
    this.clearAlerts();
  }

  viewCandidate(can: CandidateItem): void {
    this.selectedCandidate = can;
    this.showViewModal = true;
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.selectedCandidate = null;
  }

  saveCandidate(): void {
    this.clearAlerts();

    if (!this.candidateForm.name.trim() || !this.candidateForm.email.trim() || !this.candidateForm.phone.trim()) {
      this.errorMessage = 'Please complete Name, Email, and Phone fields.';
      return;
    }

    if (this.isEditing && this.currentEditId) {
      this.hrData.updateCandidate(this.currentEditId, { ...this.candidateForm });
      this.successMessage = `Candidate "${this.candidateForm.name}" updated successfully.`;
    } else {
      this.hrData.addCandidate({ ...this.candidateForm });
      this.successMessage = `Candidate "${this.candidateForm.name}" added to pipeline!`;
    }

    this.loadData();
    this.showForm = false;
    setTimeout(() => this.clearAlerts(), 4000);
  }

  deleteCandidate(id: number, name: string): void {
    if (confirm(`Are you sure you want to remove candidate "${name}"?`)) {
      this.hrData.deleteCandidate(id);
      this.loadData();
      this.successMessage = `Candidate "${name}" removed.`;
      setTimeout(() => this.clearAlerts(), 3000);
    }
  }

  clearAlerts(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }
}