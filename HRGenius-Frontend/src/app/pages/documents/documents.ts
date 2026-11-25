import { Component, OnInit } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HrDataService, DocumentItem, EmployeeItem } from '../../services/hr-data';

@Component({
  selector: 'app-documents',
  imports: [CommonModule, NgIf, NgFor, FormsModule],
  templateUrl: './documents.html',
  styleUrl: './documents.css'
})
export class Documents implements OnInit {
  documents: DocumentItem[] = [];
  employees: EmployeeItem[] = [];

  searchTerm = '';
  typeFilter = 'All';

  showForm = false;
  isEditing = false;
  currentEditId: number | null = null;

  showViewModal = false;
  selectedDocument: DocumentItem | null = null;

  successMessage = '';
  errorMessage = '';

  documentForm = {
    employee: '',
    documentName: '',
    documentType: 'Contract' as 'ID Proof' | 'Resume' | 'Contract' | 'Certificate' | 'Policy',
    documentUrl: '',
    uploadDate: '',
    status: 'Verified' as 'Verified' | 'Pending' | 'Expired'
  };

  constructor(private hrData: HrDataService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.documents = this.hrData.getDocuments();
    this.employees = this.hrData.getEmployees();
    this.hrData.fetchDocuments().subscribe({
      next: (d) => { if (d && d.length) this.documents = d; }
    });
    this.hrData.fetchEmployees().subscribe({
      next: (e) => { if (e && e.length) this.employees = e; }
    });
  }

  get filteredDocuments(): DocumentItem[] {
    return this.documents.filter(doc => {
      const q = this.searchTerm.toLowerCase();
      const matchesSearch = !this.searchTerm.trim() ||
        doc.employee?.toLowerCase().includes(q) ||
        doc.documentName?.toLowerCase().includes(q) ||
        doc.documentType?.toLowerCase().includes(q) ||
        doc.documentUrl?.toLowerCase().includes(q);

      const matchesType = this.typeFilter === 'All' || doc.documentType === this.typeFilter;

      return matchesSearch && matchesType;
    });
  }

  openAddModal(): void {
    this.isEditing = false;
    this.currentEditId = null;
    const defaultEmp = this.employees.length > 0 ? this.employees[0].name : 'David Chen';
    this.documentForm = {
      employee: defaultEmp,
      documentName: '',
      documentType: 'Contract',
      documentUrl: `/documents/${defaultEmp.toLowerCase().replace(/\s+/g, '_')}_document.pdf`,
      uploadDate: new Date().toISOString().split('T')[0],
      status: 'Verified'
    };
    this.showForm = true;
    this.clearAlerts();
  }

  openEditModal(doc: DocumentItem): void {
    this.isEditing = true;
    this.currentEditId = doc.id;
    this.documentForm = {
      employee: doc.employee,
      documentName: doc.documentName,
      documentType: doc.documentType,
      documentUrl: doc.documentUrl,
      uploadDate: doc.uploadDate,
      status: doc.status
    };
    this.showForm = true;
    this.clearAlerts();
  }

  viewDocument(doc: DocumentItem): void {
    this.selectedDocument = doc;
    this.showViewModal = true;
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.selectedDocument = null;
  }

  saveDocument(): void {
    this.clearAlerts();

    if (!this.documentForm.employee.trim() || !this.documentForm.documentName.trim() || !this.documentForm.documentUrl.trim()) {
      this.errorMessage = 'Please complete Employee, Document Name, and Document URL fields.';
      return;
    }

    if (this.isEditing && this.currentEditId) {
      this.hrData.updateDocument(this.currentEditId, { ...this.documentForm });
      this.successMessage = `Document "${this.documentForm.documentName}" updated.`;
    } else {
      this.hrData.addDocument({ ...this.documentForm });
      this.successMessage = `Document "${this.documentForm.documentName}" uploaded successfully!`;
    }

    this.loadData();
    this.showForm = false;
    setTimeout(() => this.clearAlerts(), 4000);
  }

  deleteDocument(id: number, name: string): void {
    if (confirm(`Delete document "${name}"?`)) {
      this.hrData.deleteDocument(id);
      this.loadData();
      this.successMessage = `Document "${name}" deleted.`;
      setTimeout(() => this.clearAlerts(), 3000);
    }
  }

  clearAlerts(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }
}
