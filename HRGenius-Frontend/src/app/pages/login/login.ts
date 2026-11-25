import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink, NgIf],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  username = 'admin';
  password = 'admin123';
  rememberMe = true;
  errorMessage = '';
  isLoading = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  quickFill(role: string): void {
    if (role === 'admin') {
      this.username = 'admin';
      this.password = 'admin123';
    } else if (role === 'manager') {
      this.username = 'david_lead';
      this.password = 'manager123';
    } else {
      this.username = 'jessica_dev';
      this.password = 'employee123';
    }
    this.errorMessage = '';
  }

  onSubmit(): void {
    if (!this.username.trim() || !this.password.trim()) {
      this.errorMessage = 'Please enter both username/email and password.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 401) {
          this.errorMessage = 'Invalid username or password. Please try again.';
        } else if (err.error && err.error.message) {
          this.errorMessage = err.error.message;
        } else {
          this.errorMessage = 'Unable to connect to the authentication server. Please ensure the backend is running.';
        }
      }
    });
  }
}
