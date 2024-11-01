declare const bootstrap: any;

import { Component, OnDestroy, OnInit, ViewChild, inject, signal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';

import { CalendarOptions } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import { FullCalendarComponent, FullCalendarModule } from '@fullcalendar/angular';
import { EventService } from 'app/entities/event/service/event.service';
import { FormsModule } from '@angular/forms';
// import * as bootstrap from 'bootstrap';
// import { Modal } from 'bootstrap';

interface TokenResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number;
  token_type: string;
}

@Component({
  standalone: true,
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  imports: [SharedModule, RouterModule, FullCalendarModule, FormsModule],
})
export default class HomeComponent implements OnInit, OnDestroy {
  account = signal<Account | null>(null);
  @ViewChild('calendar') calendarComponent: FullCalendarComponent | undefined;

  groupName = '';
  code: string | null = '';

  clientId: any = '2qff8uujb6qsbo2rnarmh5p2du';
  clientSecret = 'krbnaft7ri8vaqbtpteo4s9ne0';
  redirectUri = 'https://organizer-app-140b2a7a7c09.herokuapp.com/';
  tokenUrl = 'https://secure.meetup.com/oauth2/access';
  calendarOptions: CalendarOptions = {
    initialView: 'dayGridMonth',
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'customButton',
    },
    plugins: [dayGridPlugin, interactionPlugin],
    dateClick: () => this.handleDateClick(),
    weekends: true,
    customButtons: {
      customButton: {
        text: 'Add Group',
        click: () => this.openModal(),
      },
    },
    events: [],
  };

  private readonly destroy$ = new Subject<void>();

  private accountService = inject(AccountService);
  private router = inject(Router);
  private eventService = inject(EventService);

  public toggleWeekends(): void {
    this.calendarOptions.weekends = !this.calendarOptions.weekends;
  }
  handleDateClick(): void {
    const testVar = 0;
  }

  ngOnInit(): void {
    const urlParams = new URLSearchParams(window.location.search);
    this.code = urlParams.get('code');

    // eslint-disable-next-line no-console
    // console.log('Code:', this.code);

    // if (this.code) {
    //   // pass the cod
    //   this.eventService.getEventByGroupName(this.code).subscribe(events => {
    //     // eslint-disable-next-line no-console
    //     console.log('events: ', events);
    //   });
    // } else {
    //   // eslint-disable-next-line no-console
    //   console.log('No code');
    // }

    // eslint-disable-next-line no-console
    // console.log('Code:', this.code);

    // if (code) {
    //   // Store the code in session storage
    //   sessionStorage.setItem('code', code);

    //   // Clear the code from the URL (optional)
    //   window.history.replaceState({}, document.title, window.location.pathname);

    //   // Proceed to get the access token
    //   this.getAccessToken(code);
    // }

    this.accountService
      .getAuthenticationState()
      .pipe(takeUntil(this.destroy$))
      .subscribe(account => this.account.set(account));

    // eslint-disable-next-line no-console
    console.info('session storage:', sessionStorage.getItem('access_token'));
    this.loadEvents();
  }

  async getAccessToken(code: string): Promise<void> {
    try {
      const response = await fetch(this.tokenUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
          client_id: this.clientId,
          client_secret: this.clientSecret,
          grant_type: 'authorization_code',
          redirect_uri: this.redirectUri,
          code,
        }).toString(),
      });
      if (!response.ok) {
        throw new Error(`Error: ${response.status} ${response.statusText}`);
      }
      const data: TokenResponse = await response.json();
      // eslint-disable-next-line no-console
      console.info('Access Token:', data.access_token);
      sessionStorage.setItem('access_token', data.access_token);
      sessionStorage.setItem('refresh_token', data.refresh_token);
      // eslint-disable-next-line no-console
      console.info('Refresh Token:', data.refresh_token);
    } catch (error) {
      console.error('Failed to fetch access token:', error);
    }
  }

  login(): void {
    this.router.navigate(['/login']);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  openModal(): void {
    // Logic to open your modal
    const modalElement = document.getElementById('groupNameModal');
    if (modalElement) {
      // Use Bootstrap's JavaScript to show the modal
      const modal = new bootstrap.Modal(modalElement); // Use the global bootstrap object
      modal.show(); // Show the modal
    }
  }

  submitGroupName(): void {
    // eslint-disable-next-line no-console
    console.log('Code:', this.code);
    // eslint-disable-next-line no-console
    console.log('Group Name:', this.groupName);
    const modalElement = document.getElementById('groupNameModal') as HTMLElement;

    // get group name
    if (this.code) {
      // pass the code
      this.eventService.getEventByGroupName(this.code, this.groupName).subscribe(events => {
        // eslint-disable-next-line no-console
        console.log('events: ', events);
      });
    } else {
      // eslint-disable-next-line no-console
      console.log('No code');
    }
    const modal = bootstrap.Modal.getInstance(modalElement);
    modal.hide();

    // now pass that group name to the service
  }

  private loadEvents(): void {
    this.eventService
      .query()
      .pipe(takeUntil(this.destroy$))
      .subscribe(events => {
        this.calendarOptions.events = events.body?.map(value => {
          return {
            id: value.id.toString(),
            date: value.event_date?.format('YYYY-MM-DD').toString(),
            title: value.event_description ?? '',
          };
        });
      });
  }
}
