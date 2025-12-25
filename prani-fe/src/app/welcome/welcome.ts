import { Component } from '@angular/core';
import { MailFormComponent } from "../mail-form/mail-form.component";

@Component({
  selector: 'app-welcome',
  imports: [MailFormComponent],
  templateUrl: './welcome.html',
  styleUrl: './welcome.scss'
})
export class Welcome {}
