import { Component, OnInit } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HrDataService, EmployeeItem, DepartmentItem, JobItem } from '../../services/hr-data';

@Component({
  selector: 'app-employees',
  imports: [CommonModule, NgIf, NgFor, FormsModule],
  templateUrl: './employees.html',
  styleUrl: './employees.css'
})
export class Employees implements OnInit {
  employees: EmployeeItem[] = [];
  departments: DepartmentItem[] = [];
  jobs: JobItem[] = [];

  searchTerm = '';
  departmentFilter = 'All';
  statusFilter = 'All';

  showForm = false;
  isEditing = false;
  currentEditId: number | null = null;

  showViewModal = false;
  selectedEmployee: EmployeeItem | null = null;

  successMessage = '';
  errorMessage = '';

  employeeForm = {
    empId: '',
    name: '',
    email: '',
    phone: '',
    department: 'Software Engineering',
    departmentId: undefined as number | undefined,
    job: 'Frontend Developer',
    joiningDate: '',
    status: 'Active' as 'Active' | 'On Leave' | 'Terminated',
    salary: 70000,
    gender: 'Female',
    employmentType: 'Full Time',
    city: 'Austin',
    address: ''
  };

  constructor(private hrData: HrDataService) {}

  ngOnInit(): void {
    this.loadData();
    this.resetForm();
  }

  loadData(): void {
    this.employees = this.hrData.getEmployees();
    this.departments = this.hrData.getDepartments();
    this.jobs = this.hrData.getJobs();
    this.hrData.fetchEmployees().subscribe({
      next: (e) => { if (e && e.length) this.employees = e; }
    });
    this.hrData.fetchDepartments().subscribe({
      next: (d) => { if (d && d.length) this.departments = d; }
    });
    this.hrData.fetchJobs().subscribe({
      next: (j) => { if (j && j.length) this.jobs = j; }
    });
  }

  get filteredEmployees(): EmployeeItem[] {
    return this.employees.filter(emp => {
      const matchesSearch = !this.searchTerm.trim() ||
        (emp.name?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
         emp.empId?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
         emp.email?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
         emp.department?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
         emp.job?.toLowerCase().includes(this.searchTerm.toLowerCase()));

      const matchesDept = this.departmentFilter === 'All' || emp.department === this.departmentFilter;
      const matchesStatus = this.statusFilter === 'All' || emp.status === this.statusFilter;

      return matchesSearch && matchesDept && matchesStatus;
    });
  }

  openAddModal(): void {
    this.isEditing = false;
    this.currentEditId = null;
    this.resetForm();
    const nextId = this.employees.length + 1;
    this.employeeForm.empId = `EMP-${String(nextId).padStart(3, '0')}`;
    this.employeeForm.joiningDate = new Date().toISOString().split('T')[0];
    this.showForm = true;
    this.clearAlerts();
  }

  openEditModal(emp: EmployeeItem): void {
    this.isEditing = true;
    this.currentEditId = emp.id;
    this.employeeForm = {
      empId: emp.empId,
      name: emp.name,
      email: emp.email,
      phone: emp.phone,
      department: emp.department,
      departmentId: emp.departmentId,
      job: emp.job,
      joiningDate: emp.joiningDate,
      status: emp.status,
      salary: Number(emp.salary) || 60000,
      gender: emp.gender || 'Not Specified',
      employmentType: emp.employmentType || 'Full Time',
      city: emp.city || '',
      address: emp.address || ''
    };
    this.showForm = true;
    this.clearAlerts();
  }

  viewEmployee(emp: EmployeeItem): void {
    this.selectedEmployee = emp;
    this.showViewModal = true;
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.selectedEmployee = null;
  }

  resetForm(): void {
    const defaultDept = this.departments.length > 0 ? this.departments[0] : null;
    this.employeeForm = {
      empId: '',
      name: '',
      email: '',
      phone: '',
      department: defaultDept ? defaultDept.name : 'Human Resources',
      departmentId: defaultDept ? defaultDept.id : undefined,
      job: this.jobs.length > 0 ? this.jobs[0].jobTitle : 'Specialist',
      joiningDate: new Date().toISOString().split('T')[0],
      status: 'Active',
      salary: 65000,
      gender: 'Male',
      employmentType: 'Full Time',
      city: '',
      address: ''
    };
  }

  saveEmployee(): void {
    this.clearAlerts();

    if (!this.employeeForm.name.trim() || !this.employeeForm.email.trim() || !this.employeeForm.phone.trim()) {
      this.errorMessage = 'Please complete all required fields: Name, Email, and Phone.';
      return;
    }

    let selectedDeptId = this.employeeForm.departmentId;
    if (this.employeeForm.department) {
      const found = this.departments.find(d => d.name.toLowerCase() === this.employeeForm.department.toLowerCase());
      if (found) {
        selectedDeptId = found.id;
      }
    }

    const payload = {
      ...this.employeeForm,
      departmentId: selectedDeptId
    };

    if (this.isEditing && this.currentEditId) {
      this.hrData.updateEmployee(this.currentEditId, payload);
      this.successMessage = `Employee "${this.employeeForm.name}" updated successfully!`;
    } else {
      this.hrData.addEmployee(payload);
      this.successMessage = `New employee "${this.employeeForm.name}" registered successfully!`;
    }

    this.showForm = false;
    this.resetForm();

    setTimeout(() => {
      this.loadData();
    }, 200);

    setTimeout(() => this.clearAlerts(), 4000);
  }

  deleteEmployee(id: number, name: string): void {
    if (confirm(`Are you sure you want to remove employee "${name}"?`)) {
      this.hrData.deleteEmployee(id);
      this.loadData();
      this.successMessage = `Employee "${name}" has been deleted.`;
      setTimeout(() => this.clearAlerts(), 3000);
    }
  }

  clearAlerts(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }
}