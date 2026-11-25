import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet, NavigationEnd } from '@angular/router';
import { NgIf, NgClass } from '@angular/common';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { AuthService, UserSession } from '../../services/auth';
import { HrDataService } from '../../services/hr-data';

@Component({
  selector: 'app-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NgIf, NgClass],
  templateUrl: './layout.html',
  styleUrl: './layout.css'
})
export class Layout implements OnInit, OnDestroy {
  isMobileSidebarOpen = false;
  currentUser: UserSession | null = null;
  pageTitle = 'Dashboard';
  pageSubtitle = 'HR Operations, Metrics and Team Overview';
  pendingLeavesCount = 0;
  candidatesCount = 0;
  openJobsCount = 0;
  todayDateStr = '';

  private authSub?: Subscription;

  constructor(
    private authService: AuthService,
    private hrData: HrDataService,
    private router: Router
  ) {}

  get isRoleAdmin(): boolean {
    return this.authService.isAdmin();
  }

  get isRoleManager(): boolean {
    return this.authService.isManager();
  }

  get isRoleEmployee(): boolean {
    return this.authService.isEmployee();
  }

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.authSub = this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.updatePageInfo(this.router.url);
    });

    this.updateStats();
    this.updatePageInfo(this.router.url);

    const now = new Date();
    this.todayDateStr = now.toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    });

    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.updatePageInfo(event.urlAfterRedirects || event.url);
      this.isMobileSidebarOpen = false;
      this.updateStats();
    });
  }

  ngOnDestroy(): void {
    this.authSub?.unsubscribe();
  }

  updateStats(): void {
    const stats = this.hrData.getDashboardStats();
    this.pendingLeavesCount = stats.pendingLeaves;
    this.candidatesCount = stats.totalCandidates;
    this.openJobsCount = stats.openJobs;
  }

  toggleSidebar(): void {
    this.isMobileSidebarOpen = !this.isMobileSidebarOpen;
  }

  closeSidebar(): void {
    this.isMobileSidebarOpen = false;
  }

  logout(): void {
    // Clear session and navigate to login
    this.authService.logout();
  }

  private updatePageInfo(url: string): void {
    const path = url.split('?')[0].replace(/^\//, '');

    const isEmp = this.isRoleEmployee;

    const pageMeta: Record<string, { title: string; subtitle: string }> = {
      '': {
        title: isEmp ? 'My Workspace' : 'Dashboard',
        subtitle: isEmp ? 'Personal Employee Portal, Time Logs & Tasks' : 'HR Operations, Metrics and Team Overview'
      },
      'dashboard': {
        title: isEmp ? 'My Workspace' : 'Dashboard',
        subtitle: isEmp ? 'Personal Employee Portal, Time Logs & Tasks' : 'HR Operations, Metrics and Team Overview'
      },
      'employees': {
        title: 'Employees Directory',
        subtitle: isEmp ? 'Staff Directory & Team Contacts' : 'Directory, Staff Details and Profile Records'
      },
      'departments': {
        title: 'Departments',
        subtitle: 'Organizational Units, Structure and Division Heads'
      },
      'jobs': {
        title: isEmp ? 'Open Positions' : 'Jobs & Openings',
        subtitle: isEmp ? 'Internal Job Openings and Career Pathways' : 'Career Positions, Job Descriptions and Requirements'
      },
      'candidates': {
        title: 'Candidates Pipeline',
        subtitle: 'Applicant Tracking, Pipeline Stages and Resume Profiles'
      },
      'attendance': {
        title: isEmp ? 'My Attendance' : 'Attendance',
        subtitle: isEmp ? 'Daily Clock-in History and Timesheets' : 'Daily Time Logs, Check-in Tracking and Status'
      },
      'leaves': {
        title: isEmp ? 'My Leave Requests' : 'Leave Management',
        subtitle: isEmp ? 'Time-off Requests, Balances and Request Submission' : 'Time-off Requests, Balances and Approval Queue'
      },
      'payroll': {
        title: isEmp ? 'My Salary & Payslips' : 'Payroll & Compensation',
        subtitle: isEmp ? 'Monthly Salary Slips, Allowances and Deductions' : 'Compensation Slips, Deductions and Monthly Disbursement'
      },
      'performance': {
        title: isEmp ? 'My Performance' : 'Performance Reviews',
        subtitle: isEmp ? 'Quarterly Evaluations, Goals and Achievements' : 'Quarterly Ratings, Goals and Staff Appraisals'
      },
      'interviews': {
        title: 'Interview Schedule',
        subtitle: 'Recruitment Schedule, Candidate Meetings and Feedback'
      },
      'training': {
        title: isEmp ? 'My Training Programs' : 'Training & Development',
        subtitle: isEmp ? 'Assigned Courses, Upskilling and Completion Status' : 'Upskilling Programs, Schedules and Employee Enrollment'
      },
      'documents': {
        title: isEmp ? 'My Documents' : 'Documents Repository',
        subtitle: isEmp ? 'My Uploaded Verification Files & Documents' : 'Employee Records, Verification Files and ID Documents'
      },
      'users': {
        title: 'User Management',
        subtitle: 'System Access Roles, Admin Accounts and Status'
      },
      'profile': {
        title: isEmp ? 'My Profile' : 'User Profile',
        subtitle: isEmp ? 'Personal Employment Record & Account Details' : 'Account Credentials, Permissions and Staff Details'
      }
    };

    if (pageMeta[path]) {
      this.pageTitle = pageMeta[path].title;
      this.pageSubtitle = pageMeta[path].subtitle;
    } else {
      this.pageTitle = 'HRGenius System';
      this.pageSubtitle = 'Human Resource Management';
    }
  }
}
