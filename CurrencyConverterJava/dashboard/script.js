const API_BASE="https://open.er-api.com/v6/latest/";
const HISTORY_BASE="https://api.frankfurter.app";
const currencies=["INR","USD","EUR","GBP","JPY","AUD","CAD","CHF","CNY","AED","SGD","NZD","HKD","KRW","THB","MYR","ZAR","BRL","MXN","SEK","NOK"];
const popular=["USD","EUR","GBP","JPY","AED","AUD","CAD","SGD","CHF","CNY"];
let rates={};let lastUpdated="";let nextUpdated="";let showingAll=false;

const $=id=>document.getElementById(id);
function fillSelect(select,values){select.innerHTML=values.map(c=>`<option value="${c}">${c}</option>`).join("")}
function setStatus(ok){$("statusBadge").classList.toggle("online",ok);$("statusBadge").innerHTML=ok?"<i></i> Live":"<i></i> Offline"}

async function loadRates(){
  setStatus(false);$("refreshBtn").disabled=true;
  try{
    const res=await fetch(API_BASE+"INR",{cache:"no-store"});
    if(!res.ok)throw new Error("Rate API unavailable");
    const data=await res.json();
    if(data.result!=="success")throw new Error("Rate API returned an error");
    rates=data.rates||{};lastUpdated=data.time_last_update_utc||"Unavailable";nextUpdated=data.time_next_update_utc||"Unavailable";
    $("usdRate").textContent=fmt(rates.USD);$("eurRate").textContent=fmt(rates.EUR);
    $("updatedAt").textContent=shortDate(lastUpdated);$("nextUpdate").textContent="Next: "+shortDate(nextUpdated);
    renderRates();convert();setStatus(true);loadTrend();
  }catch(e){setStatus(false);$("conversionText").textContent="Unable to load live rates";$("conversionRate").textContent=e.message}
  finally{$("refreshBtn").disabled=false}
}
function fmt(n){return typeof n==="number"?n.toFixed(6):"—"}
function shortDate(s){if(!s||s==="Unavailable")return "—";const d=new Date(s);return isNaN(d)?s.replace("UTC","").trim():d.toLocaleString([], {dateStyle:"medium",timeStyle:"short"})}
function convert(){
  const amount=Number($("amount").value)||0,from=$("from").value,to=$("to").value;
  if(!rates[to]||!rates[from])return;
  const cross=from==="INR"?rates[to]:to==="INR"?1/rates[from]:rates[to]/rates[from];
  const result=amount*cross;
  $("conversionText").textContent=`${amount.toLocaleString()} ${from} = ${result.toLocaleString(undefined,{maximumFractionDigits:2})} ${to}`;
  $("conversionRate").textContent=`1 ${from} = ${cross.toFixed(6)} ${to}`;
}
function renderRates(){
  const list=showingAll?currencies.filter(c=>c!=="INR"):popular;
  $("rateGrid").innerHTML=list.map(c=>`<div class="rate-card"><div class="pair">INR → ${c}</div><strong>${fmt(rates[c])}</strong><small>1 INR</small></div>`).join("");
  $("showAllBtn").textContent=showingAll?"Show popular":"Show all";
}
async function loadTrend(){
  const target=$("trendCurrency").value;
  if(!target)return;
  const end=new Date(),start=new Date();start.setDate(end.getDate()-7);
  const f=d=>d.toISOString().slice(0,10);
  try{
    const res=await fetch(`${HISTORY_BASE}/${f(start)}..${f(end)}?from=INR&to=${target}`);
    if(!res.ok)throw new Error("History unavailable");
    const data=await res.json();
    const points=Object.entries(data.rates||{}).map(([date,v])=>({date,value:v[target]}));
    drawChart(points,target);
    $("trendNote").textContent=`INR → ${target}, last 7 days • Source: Frankfurter`;
  }catch(e){$("trendNote").textContent="Historical data could not be loaded.";drawChart([],target)}
}
function drawChart(points,target){
  const canvas=$("trendChart"),ctx=canvas.getContext("2d"),dpr=window.devicePixelRatio||1;
  const rect=canvas.getBoundingClientRect();canvas.width=rect.width*dpr;canvas.height=260*dpr;ctx.scale(dpr,dpr);
  const w=rect.width,h=260,p={l:42,r:18,t:18,b:32};ctx.clearRect(0,0,w,h);
  if(points.length<2){ctx.fillStyle=getComputedStyle(document.body).getPropertyValue("--muted");ctx.font="13px system-ui";ctx.fillText("No trend data available",p.l,h/2);return}
  const vals=points.map(x=>x.value),min=Math.min(...vals),max=Math.max(...vals),range=max-min||1;
  const x=i=>p.l+i*(w-p.l-p.r)/(points.length-1),y=v=>p.t+(max-v)*(h-p.t-p.b)/range;
  ctx.strokeStyle=getComputedStyle(document.body).getPropertyValue("--line");ctx.lineWidth=1;
  for(let i=0;i<4;i++){const yy=p.t+i*(h-p.t-p.b)/3;ctx.beginPath();ctx.moveTo(p.l,yy);ctx.lineTo(w-p.r,yy);ctx.stroke()}
  ctx.strokeStyle=getComputedStyle(document.body).getPropertyValue("--accent");ctx.lineWidth=3;ctx.beginPath();
  points.forEach((pt,i)=>i?ctx.lineTo(x(i),y(pt.value)):ctx.moveTo(x(i),y(pt.value)));ctx.stroke();
  ctx.fillStyle=getComputedStyle(document.body).getPropertyValue("--accent");
  points.forEach((pt,i)=>{ctx.beginPath();ctx.arc(x(i),y(pt.value),4,0,Math.PI*2);ctx.fill()});
  ctx.fillStyle=getComputedStyle(document.body).getPropertyValue("--muted");ctx.font="10px system-ui";
  points.forEach((pt,i)=>{if(i===0||i===points.length-1)ctx.fillText(pt.date.slice(5),x(i)-12,h-10)});
}
$("from")&&fillSelect($("from"),currencies);fillSelect($("to"),currencies);fillSelect($("trendCurrency"),popular);
$("from").value="INR";$("to").value="USD";$("trendCurrency").value="USD";
$("amount").addEventListener("input",convert);$("from").addEventListener("change",convert);$("to").addEventListener("change",convert);
$("swapBtn").addEventListener("click",()=>{const a=$("from").value;$("from").value=$("to").value;$("to").value=a;convert()});
$("refreshBtn").addEventListener("click",loadRates);$("trendCurrency").addEventListener("change",loadTrend);
$("showAllBtn").addEventListener("click",()=>{showingAll=!showingAll;renderRates()});
$("themeBtn").addEventListener("click",()=>{document.body.classList.toggle("dark");localStorage.setItem("dashboardTheme",document.body.classList.contains("dark")?"dark":"light");if(rates.USD)loadTrend()});
if(localStorage.getItem("dashboardTheme")==="dark")document.body.classList.add("dark");
window.addEventListener("resize",()=>{if(rates.USD)loadTrend()});
loadRates();