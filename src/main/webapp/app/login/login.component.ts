import { AfterViewInit, Component, ElementRef, OnInit, inject, signal, viewChild } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { LoginService } from 'app/login/login.service';
import { AccountService } from 'app/core/auth/account.service';

@Component({
  standalone: true,
  selector: 'jhi-login',
  imports: [SharedModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.component.html',
})
export default class LoginComponent implements OnInit, AfterViewInit {
  username = viewChild.required<ElementRef>('username');
  clientId: any = '2qff8uujb6qsbo2rnarmh5p2du';
  clientSecret = 'krbnaft7ri8vaqbtpteo4s9ne0';
  // redirectUri = 'https://b10315c1b7cd.ngrok.app/';
  redirectUri = 'https://organizer-app-140b2a7a7c09.herokuapp.com/';

  // code = '3ae658e5a776fcf2f2db975e2dbb72e1';

  tokenUrl = 'https://secure.meetup.com/oauth2/access';

  authenticationError = signal(false);

  loginForm = new FormGroup({
    username: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    rememberMe: new FormControl(false, { nonNullable: true, validators: [Validators.required] }),
  });

  private accountService = inject(AccountService);
  private loginService = inject(LoginService);
  private router = inject(Router);

  private meetupAuthUrl =
    // 'https://secure.meetup.com/oauth2/authorize?client_id=2qff8uujb6qsbo2rnarmh5p2du&response_type=code&redirect_uri=https://organizer-app-140b2a7a7c09.herokuapp.com/';
    'https://secure.meetup.com/oauth2/authorize?client_id=2qff8uujb6qsbo2rnarmh5p2du&response_type=code&redirect_uri=https://b10315c1b7cd.ngrok.app/';

  ngOnInit(): void {
    // if already authenticated then navigate to home page
    this.accountService.identity().subscribe(() => {
      if (this.accountService.isAuthenticated()) {
        this.router.navigate(['']);
      }
    });
  }

  ngAfterViewInit(): void {
    this.username().nativeElement.focus();
  }

  login(): void {
    this.loginService.login(this.loginForm.getRawValue()).subscribe({
      next: () => {
        this.authenticationError.set(false);
        if (!this.router.getCurrentNavigation()) {
          // There were no routing during login (eg from navigationToStoredUrl)
          window.location.href = this.meetupAuthUrl;

          // get code and store in local storage
          // store on local storage
          // After redirection, to get the code parameter from the URL:
          const urlParams = new URLSearchParams(window.location.search);
          const code: any = urlParams.get('code');

          sessionStorage.setItem('code', code);

          // eslint-disable-next-line no-console
          console.info('code: ', code);
        }
      },
      error: () => this.authenticationError.set(true),
    });
  }
}
