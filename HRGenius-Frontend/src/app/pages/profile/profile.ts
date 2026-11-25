import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, NgIf, NgClass } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService, ProfileResponse, User } from '../../services/auth';
import { HrDataService, EmployeeItem } from '../../services/hr-data';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, RouterLink, NgIf, NgClass],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class Profile implements OnInit {
  currentUser: User | null = null;
  profile: ProfileResponse | null = null;
  isLoading = true;
  errorMessage = '';
  activeTab: 'overview' | 'employment' | 'security' = 'overview';

  constructor(
    private authService: AuthService,
    private hrData: HrDataService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    // Initialize profile immediately with available session data for instant render
    this.fallbackToSession();
    if (this.profile) {
      this.isLoading = false;
    }
    this.loadProfile();
  }

  loadProfile(): void {
    if (!this.profile) {
      this.isLoading = true;
    }
    this.errorMessage = '';

    this.authService.getProfile().subscribe({
      next: (res) => {
        if (res) {
          this.profile = res;
        } else {
          this.fallbackToSession();
        }
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.warn('Failed to load profile from backend endpoint, using session fallback', err);
        if (!this.profile) {
          this.fallbackToSession();
        }
        this.isLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  private fallbackToSession(): void {
    const user = this.currentUser;
    const name = user?.name || user?.username || 'Current User';
    const email = user?.email || 'user@hrgenius.com';
    const role = user?.role || 'Employee';

    // Find linked employee from local store if available
    const emps = this.hrData.getEmployees();
    const matched = emps.find(e =>
      (e.email && e.email.toLowerCase() === email.toLowerCase()) ||
      (e.name && e.name.toLowerCase() === name.toLowerCase()) ||
      (user?.empId && e.empId && e.empId.toLowerCase() === user.empId.toLowerCase())
    );

    this.profile = {
      id: user?.id || 1,
      username: user?.username || 'user',
      name: matched?.name || name,
      email: matched?.email || email,
      role: role,
      status: matched?.status || 'Active',
      empId: matched?.empId || user?.empId || 'EMP-001',
      employeeId: matched?.id,
      department: matched?.department || (this.isRoleAdmin ? 'System Administration' : 'Software Engineering'),
      departmentId: matched?.departmentId,
      job: matched?.job || matched?.designation || (this.isRoleAdmin ? 'System Administrator' : 'Staff Associate'),
      designation: matched?.designation || matched?.job || 'Staff Associate',
      phone: matched?.phone || '+1 (555) 012-3456',
      joiningDate: matched?.joiningDate || '2023-01-15',
      salary: matched?.salary ? Number(matched.salary) : 75000,
      gender: matched?.gender || 'Not Specified',
      employmentType: matched?.employmentType || 'Full Time',
      city: matched?.city || 'Austin',
      address: matched?.address || 'Company Headquarters',
      avatar: user?.avatar || name.charAt(0).toUpperCase()
    };
  }

  get isRoleAdmin(): boolean {
    return this.authService.isAdmin();
  }

  get isRoleManager(): boolean {
    return this.authService.isManager();
  }

  get isRoleEmployee(): boolean {
    return this.authService.isEmployee();
  }

  get normalizedRoleBadge(): { label: string; class: string } {
    if (this.isRoleAdmin) {
      return { label: 'Administrator', class: 'bg-purple-100 text-purple-800 border-purple-200' };
    }
    if (this.isRoleManager) {
      return { label: 'HR Manager / Lead', class: 'bg-blue-100 text-blue-800 border-blue-200' };
    }
    return { label: 'Staff Employee', class: 'bg-emerald-100 text-emerald-800 border-emerald-200' };
  }

  formatCurrency(amount: number | string | undefined | null): string {
    if (amount === undefined || amount === null || amount === '') return '$0';
    const num = typeof amount === 'string' ? parseFloat(amount) : amount;
    return isNaN(num) ? '$0' : new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(num);
  }
}
